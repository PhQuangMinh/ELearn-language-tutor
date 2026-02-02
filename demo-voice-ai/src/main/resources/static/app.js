// WAV recorder (PCM 16-bit mono @ 16kHz) + submit to /api/voice/assess
// No external deps.

const els = {
  language: document.getElementById("language"),
  referenceText: document.getElementById("referenceText"),
  maxSeconds: document.getElementById("maxSeconds"),
  btnStart: document.getElementById("btnStart"),
  btnStop: document.getElementById("btnStop"),
  btnSend: document.getElementById("btnSend"),
  recordState: document.getElementById("recordState"),
  timer: document.getElementById("timer"),
  player: document.getElementById("player"),
  error: document.getElementById("error"),
  loading: document.getElementById("loading"),
  resultCard: document.getElementById("resultCard"),
  rStatus: document.getElementById("rStatus"),
  rText: document.getElementById("rText"),
  overallScores: document.getElementById("overallScores"),
  words: document.getElementById("words"),
  rawJson: document.getElementById("rawJson"),
};

const TARGET_SAMPLE_RATE = 16000;

let mediaStream = null;
let audioContext = null;
let sourceNode = null;
let processorNode = null;

let isRecording = false;
let startedAt = 0;
let timerId = null;
let autoStopId = null;

let recordedWavBlob = null;
let recordedUrl = null;

function showError(msg) {
  els.error.hidden = !msg;
  els.error.textContent = msg || "";
}

function setLoading(on) {
  els.loading.hidden = !on;
  els.btnSend.disabled = on || !recordedWavBlob;
  els.btnStart.disabled = on || isRecording;
  els.btnStop.disabled = on || !isRecording;
}

function setState(text) {
  els.recordState.textContent = text;
}

function formatSeconds(s) {
  const t = Math.max(0, s);
  return `${t.toFixed(1).padStart(4, "0")}s`;
}

function updateTimer() {
  const sec = (performance.now() - startedAt) / 1000;
  els.timer.textContent = formatSeconds(sec);
}

function resetAudioPreview() {
  if (recordedUrl) URL.revokeObjectURL(recordedUrl);
  recordedUrl = null;
  recordedWavBlob = null;
  els.player.removeAttribute("src");
  els.player.load();
  els.btnSend.disabled = true;
}

function clamp16Bit(x) {
  const v = Math.max(-1, Math.min(1, x));
  return v < 0 ? v * 0x8000 : v * 0x7fff;
}

function downsampleFloat32(buffer, inSampleRate, outSampleRate) {
  if (outSampleRate === inSampleRate) return buffer;
  if (outSampleRate > inSampleRate) {
    throw new Error("Không hỗ trợ upsample.");
  }

  const ratio = inSampleRate / outSampleRate;
  const outLength = Math.round(buffer.length / ratio);
  const out = new Float32Array(outLength);

  let offset = 0;
  for (let i = 0; i < outLength; i++) {
    const nextOffset = Math.round((i + 1) * ratio);
    // average between offset..nextOffset
    let sum = 0;
    let count = 0;
    for (let j = offset; j < nextOffset && j < buffer.length; j++) {
      sum += buffer[j];
      count++;
    }
    out[i] = count ? sum / count : 0;
    offset = nextOffset;
  }
  return out;
}

function floatTo16BitPCM(float32) {
  const out = new Int16Array(float32.length);
  for (let i = 0; i < float32.length; i++) {
    out[i] = clamp16Bit(float32[i]);
  }
  return out;
}

function writeWavHeader(view, sampleRate, numChannels, numFrames) {
  // RIFF header for PCM 16-bit
  const bytesPerSample = 2;
  const blockAlign = numChannels * bytesPerSample;
  const byteRate = sampleRate * blockAlign;
  const dataSize = numFrames * blockAlign;

  function writeString(offset, str) {
    for (let i = 0; i < str.length; i++) {
      view.setUint8(offset + i, str.charCodeAt(i));
    }
  }

  writeString(0, "RIFF");
  view.setUint32(4, 36 + dataSize, true);
  writeString(8, "WAVE");
  writeString(12, "fmt ");
  view.setUint32(16, 16, true); // PCM chunk size
  view.setUint16(20, 1, true); // PCM format
  view.setUint16(22, numChannels, true);
  view.setUint32(24, sampleRate, true);
  view.setUint32(28, byteRate, true);
  view.setUint16(32, blockAlign, true);
  view.setUint16(34, 16, true); // bits per sample
  writeString(36, "data");
  view.setUint32(40, dataSize, true);
}

function encodeWavFromBuffers(floatBuffers, inputSampleRate) {
  // concat float buffers
  const totalLength = floatBuffers.reduce((a, b) => a + b.length, 0);
  const merged = new Float32Array(totalLength);
  let pos = 0;
  for (const b of floatBuffers) {
    merged.set(b, pos);
    pos += b.length;
  }

  const down = downsampleFloat32(merged, inputSampleRate, TARGET_SAMPLE_RATE);
  const pcm16 = floatTo16BitPCM(down);

  const wavBytes = new ArrayBuffer(44 + pcm16.length * 2);
  const view = new DataView(wavBytes);
  writeWavHeader(view, TARGET_SAMPLE_RATE, 1, pcm16.length);

  let offset = 44;
  for (let i = 0; i < pcm16.length; i++, offset += 2) {
    view.setInt16(offset, pcm16[i], true);
  }
  return new Blob([view], { type: "audio/wav" });
}

async function startRecording() {
  showError("");
  els.resultCard.hidden = true;
  resetAudioPreview();

  const maxSec = Number(els.maxSeconds.value || "5");
  if (!Number.isFinite(maxSec) || maxSec <= 0) {
    showError("maxSeconds không hợp lệ.");
    return;
  }

  mediaStream = await navigator.mediaDevices.getUserMedia({
    audio: {
      channelCount: 1,
      echoCancellation: true,
      noiseSuppression: true,
      autoGainControl: true,
    },
  });

  audioContext = new (window.AudioContext || window.webkitAudioContext)();
  sourceNode = audioContext.createMediaStreamSource(mediaStream);

  // ScriptProcessorNode is deprecated but widely supported; good enough for demo.
  // Use small-ish buffer size for stable capture.
  processorNode = audioContext.createScriptProcessor(4096, 1, 1);

  const inputSampleRate = audioContext.sampleRate;
  const chunks = [];

  processorNode.onaudioprocess = (e) => {
    if (!isRecording) return;
    const ch0 = e.inputBuffer.getChannelData(0);
    // copy buffer, because underlying data is reused
    chunks.push(new Float32Array(ch0));
  };

  sourceNode.connect(processorNode);
  processorNode.connect(audioContext.destination);

  isRecording = true;
  setState("Recording…");
  els.btnStart.disabled = true;
  els.btnStop.disabled = false;
  els.btnSend.disabled = true;

  startedAt = performance.now();
  els.timer.textContent = "00.0s";
  timerId = window.setInterval(updateTimer, 100);

  autoStopId = window.setTimeout(() => stopRecording(chunks, inputSampleRate), maxSec * 1000);

  // keep reference on window for stop button
  window.__recChunks = chunks;
  window.__recSampleRate = inputSampleRate;
}

async function stopRecording(chunksArg, inputSampleRateArg) {
  if (!isRecording) return;
  isRecording = false;
  setState("Processing…");
  els.btnStop.disabled = true;

  if (timerId) window.clearInterval(timerId);
  timerId = null;
  if (autoStopId) window.clearTimeout(autoStopId);
  autoStopId = null;
  updateTimer();

  const chunks = chunksArg || window.__recChunks || [];
  const inputSampleRate = inputSampleRateArg || window.__recSampleRate || 48000;

  try {
    recordedWavBlob = encodeWavFromBuffers(chunks, inputSampleRate);
    recordedUrl = URL.createObjectURL(recordedWavBlob);
    els.player.src = recordedUrl;
    els.player.load();
    setState("Ready");
    els.btnSend.disabled = false;
  } catch (e) {
    showError(`Không encode được WAV: ${e?.message || e}`);
    setState("Idle");
    resetAudioPreview();
  } finally {
    // cleanup audio graph + stream
    try { processorNode && processorNode.disconnect(); } catch {}
    try { sourceNode && sourceNode.disconnect(); } catch {}
    try { audioContext && audioContext.close(); } catch {}
    processorNode = null;
    sourceNode = null;
    audioContext = null;

    if (mediaStream) {
      mediaStream.getTracks().forEach((t) => t.stop());
      mediaStream = null;
    }

    els.btnStart.disabled = false;
  }
}

function scoreTag(value) {
  if (typeof value !== "number" || !Number.isFinite(value)) return "tag";
  if (value >= 85) return "tag good";
  if (value >= 70) return "tag warn";
  return "tag bad";
}

function renderOverall(overall) {
  const rows = [
    ["Accuracy", overall?.accuracyScore],
    ["Fluency", overall?.fluencyScore],
    ["Prosody", overall?.prosodyScore],
    ["Completeness", overall?.completenessScore],
    ["PronScore", overall?.pronScore],
  ];
  els.overallScores.innerHTML = rows
    .map(([label, v]) => {
      const val = typeof v === "number" ? v.toFixed(1) : "-";
      return `
        <div class="score">
          <div class="label">${label}</div>
          <div class="value">${val}</div>
        </div>
      `;
    })
    .join("");
}

function renderMiniList(items, kind) {
  if (!Array.isArray(items) || items.length === 0) return `<div class="miniList"><div>(none)</div></div>`;
  return `
    <div class="miniList">
      ${items
        .map((x) => {
          const name = kind === "syllable" ? (x.syllable ?? "") : (x.phoneme ?? "");
          const acc = typeof x.accuracyScore === "number" ? x.accuracyScore.toFixed(1) : "-";
          return `
            <div><b>${name || "∅"}</b></div>
            <div>acc: <b>${acc}</b></div>
            <div>off: <b>${x.offset ?? "-"}</b></div>
          `;
        })
        .join("")}
    </div>
  `;
}

function renderWords(words) {
  if (!Array.isArray(words) || words.length === 0) {
    els.words.innerHTML = `<div class="meta">Không có words trong response.</div>`;
    return;
  }

  els.words.innerHTML = words
    .map((w, idx) => {
      const acc = typeof w.accuracyScore === "number" ? w.accuracyScore.toFixed(1) : "-";
      const err = w.errorType || "Unknown";
      return `
        <div class="word">
          <div class="wordHead">
            <div>
              <div class="wordTitle">${idx + 1}. ${escapeHtml(w.word || "(null)")}</div>
              <div class="meta">offset: ${w.offset ?? "-"} • duration: ${w.duration ?? "-"}</div>
            </div>
            <div class="${scoreTag(w.accuracyScore)}">acc ${acc} • ${escapeHtml(err)}</div>
          </div>

          <div class="subtable">
            <div class="mini">
              <div class="miniTitle">Syllables</div>
              ${renderMiniList(w.syllables, "syllable")}
            </div>
            <div class="mini">
              <div class="miniTitle">Phonemes</div>
              ${renderMiniList(w.phonemes, "phoneme")}
            </div>
          </div>
        </div>
      `;
    })
    .join("");
}

function escapeHtml(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

async function sendToApi() {
  showError("");
  els.resultCard.hidden = true;

  const referenceText = (els.referenceText.value || "").trim();
  if (!referenceText) {
    showError("Bạn chưa nhập referenceText.");
    return;
  }
  if (!recordedWavBlob) {
    showError("Chưa có audio để gửi. Hãy ghi âm trước.");
    return;
  }

  const fd = new FormData();
  fd.append("audio", recordedWavBlob, "recording.wav");
  fd.append("referenceText", referenceText);
  fd.append("language", els.language.value);

  setLoading(true);
  try {
    const res = await fetch("/api/voice/assess", { method: "POST", body: fd });
    const text = await res.text();
    const data = text ? JSON.parse(text) : null;

    if (!res.ok) {
      const msg = data?.message || data?.error || `HTTP ${res.status}`;
      showError(msg);
      return;
    }

    els.rStatus.textContent = data?.recognitionStatus ?? "-";
    els.rText.textContent = data?.displayText ?? "-";
    renderOverall(data?.overall);
    renderWords(data?.words);
    els.rawJson.textContent = data?.rawJson ? JSON.stringify(JSON.parse(data.rawJson), null, 2) : "-";

    els.resultCard.hidden = false;
  } catch (e) {
    showError(`Gọi API lỗi: ${e?.message || e}`);
  } finally {
    setLoading(false);
  }
}

els.btnStart.addEventListener("click", async () => {
  try {
    await startRecording();
  } catch (e) {
    showError(`Không truy cập được microphone: ${e?.message || e}`);
    setState("Idle");
    els.btnStart.disabled = false;
    els.btnStop.disabled = true;
  }
});

els.btnStop.addEventListener("click", async () => {
  await stopRecording(window.__recChunks, window.__recSampleRate);
});

els.btnSend.addEventListener("click", async () => {
  await sendToApi();
});

// init
setState("Idle");
els.timer.textContent = "00.0s";
els.btnStop.disabled = true;
els.btnSend.disabled = true;
setLoading(false);
showError("");

