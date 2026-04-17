package com.nhom2.elearnlanguage.presentation.ui.main_app.home.profile

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.data.source.local.ThemeManager
import com.nhom2.elearnlanguage.databinding.FragmentProfileBinding
import com.nhom2.elearnlanguage.databinding.DialogLogoutAccountBinding
import com.nhom2.elearnlanguage.domain.model.UserProfile
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    companion object {
        private const val MAX_AVATAR_BYTES = 10 * 1024 * 1024
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private var selectedAvatarUri: Uri? = null
    private var selectedAvatarName: String? = null
    private var selectedAvatarMimeType: String? = null
    private var lastRenderedProfile: UserProfile? = null
    private var isThemeSwitchBinding = false

    private val pickAvatarLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            selectedAvatarUri = uri
            selectedAvatarName = resolveDisplayName(uri)
            selectedAvatarMimeType = requireContext().contentResolver.getType(uri)
            renderAvatar(uri)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        viewModel.loadProfile()
    }

    private fun setupView() {
        binding.etEmail.keyListener = null
        binding.avatarCard.setOnClickListener { pickAvatarLauncher.launch("image/*") }
        binding.tvChangeAvatar.setOnClickListener { pickAvatarLauncher.launch("image/*") }
        binding.tvChangePassword.setOnClickListener { openChangePasswordScreen() }
        binding.tvLogoutAccount.setOnClickListener { showLogoutDialog() }
        binding.btnSave.setOnClickListener { onSaveClicked() }
        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        isThemeSwitchBinding = true
        binding.switchTheme.isChecked = ThemeManager.isDarkMode(requireContext())
        isThemeSwitchBinding = false

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isThemeSwitchBinding) return@setOnCheckedChangeListener
            ThemeManager.setDarkMode(requireContext(), isChecked)
        }
    }

    private fun openChangePasswordScreen() {
        requireParentFragment().childFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .hide(this)
            .add(R.id.flTabContainer, ChangePasswordFragment())
            .addToBackStack("change_password")
            .commit()
    }

    private fun showLogoutDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogLogoutAccountBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setDimAmount(0.55f)
        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
            sheet.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.from(sheet).skipCollapsed = true
        }

        dialogBinding.btnConfirmLogout.setOnClickListener {
            dialog.dismiss()
            performLogout()
        }
        dialogBinding.tvCancelLogout.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun performLogout() {
        viewModel.logout()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.btnSave.isEnabled = !state.isLoading && !state.isSaving && !state.isLoggingOut
                    binding.tvLogoutAccount.isEnabled = !state.isLoggingOut

                    val profile = state.profile
                    if (profile != null && profile != lastRenderedProfile) {
                        bindProfile(profile)
                        lastRenderedProfile = profile
                    }
                    bindStreak(state.currentStreak, state.longestStreak)

                    state.errorMessage?.let { message ->
                        handleErrorMessage(message)
                        viewModel.consumeMessages()
                    }

                    state.successMessage?.let { message ->
                        selectedAvatarUri = null
                        selectedAvatarName = null
                        selectedAvatarMimeType = null
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        viewModel.consumeMessages()
                    }

                    if (state.logoutCompleted) {
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, true)
                            .build()
                        requireActivity()
                            .findNavController(R.id.nav_host_fragment)
                            .navigate(R.id.loginFragment, null, navOptions)
                        viewModel.consumeLogoutEvent()
                    }
                }
            }
        }
    }

    private fun bindProfile(profile: UserProfile) {
        binding.tvDisplayName.text = profile.fullName
        binding.etName.setText(profile.fullName)
        binding.etEmail.setText(profile.email)
        binding.tvChangePassword.visibility =
            if (profile.provider.equals("google", ignoreCase = true)) View.GONE else View.VISIBLE

        if (selectedAvatarUri == null) {
            val avatarUrl = profile.avatarUrl?.trim()
            if (avatarUrl.isNullOrBlank()) {
                renderDefaultAvatar()
            } else {
                renderAvatar(avatarUrl)
            }
        }
    }

    private fun bindStreak(currentStreak: Int?, longestStreak: Int?) {
        binding.tvCurrentStreakValue.text = currentStreak?.toString()
            ?: getString(R.string.profile_streak_default_value)
        binding.tvLongestStreakValue.text = longestStreak?.toString()
            ?: getString(R.string.profile_streak_default_value)
    }

    private fun onSaveClicked() {
        val fullNameInput = binding.etName.text?.toString().orEmpty()
        val validationMessage = validateName(fullNameInput)
        if (validationMessage != null) {
            binding.etName.error = validationMessage
            return
        }

        binding.etName.error = null
        val avatarBytes = selectedAvatarUri?.let { readAvatarBytes(it) }
        if (selectedAvatarUri != null && avatarBytes == null) {
            Toast.makeText(requireContext(), getString(R.string.profile_avatar_read_failed), Toast.LENGTH_SHORT).show()
            return
        }
        if (avatarBytes != null && avatarBytes.size > MAX_AVATAR_BYTES) {
            Toast.makeText(requireContext(), getString(R.string.profile_avatar_too_large), Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateProfile(
            fullName = fullNameInput.trim(),
            avatarBytes = avatarBytes,
            avatarFileName = selectedAvatarName,
            avatarMimeType = selectedAvatarMimeType
        )
    }

    private fun validateName(rawName: String): String? {
        val name = rawName.trim()
        if (name.isEmpty()) {
            return getString(R.string.profile_name_empty_error)
        }
        if (name.length < 4) {
            return getString(R.string.profile_name_too_short_error)
        }
        return null
    }

    private fun handleErrorMessage(message: String) {
        when (message) {
            getString(R.string.profile_name_empty_error),
            getString(R.string.profile_name_too_short_error) -> binding.etName.error = message
            else -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun readAvatarBytes(uri: Uri): ByteArray? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) {
                return it.getString(nameIndex)
            }
        }
        return null
    }

    private fun renderAvatar(source: Any) {
        if (source is String && source.isBlank()) {
            renderDefaultAvatar()
            return
        }
        binding.ivAvatar.setPadding(0, 0, 0, 0)
        binding.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.ivAvatar.imageTintList = null
        binding.ivAvatar.load(source) {
            crossfade(true)
            error(R.drawable.ic_person_outline)
            placeholder(R.drawable.ic_person_outline)
        }
    }

    private fun renderDefaultAvatar() {
        val padding = (22 * resources.displayMetrics.density).toInt()
        binding.ivAvatar.setPadding(padding, padding, padding, padding)
        binding.ivAvatar.scaleType = ImageView.ScaleType.CENTER_INSIDE
        binding.ivAvatar.load(R.drawable.ic_person_outline)
        binding.ivAvatar.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
