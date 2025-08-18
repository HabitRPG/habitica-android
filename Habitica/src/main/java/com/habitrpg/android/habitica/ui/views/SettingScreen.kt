package com.habitrpg.android.habitica.ui.views

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.theme.HabiticaTheme

@Composable
fun ConfigurableFormScreen(cfg: FormScreenConfig) {
    val colors = HabiticaTheme.colors

    var values by remember { mutableStateOf(cfg.fields.associate { it.key to it.initialValue }) }
    var attempted by remember { mutableStateOf(false) }
    val touchedFields = remember { mutableStateMapOf<String, Boolean>() }

    val errorRes: Map<String, Int?> = remember(values, attempted, touchedFields) {
        cfg.fields.associate { f ->
            val show = attempted || (touchedFields[f.key] == true)
            val rawError: Int? = when (f.key) {
                "confirmPw" -> {
                    if (show && values["confirmPw"].orEmpty() != values["newPw"].orEmpty()) {
                        R.string.password_not_matching
                    } else {
                        f.validator(values[f.key].orEmpty())
                    }
                }

                else -> f.validator(values[f.key].orEmpty())
            }
            f.key to if (show) rawError else null
        }
    }

    val errors: Map<String, String?> = errorRes.mapValues { (key, resId) ->
        resId?.let {
            if (it == R.string.password_too_short) {
                stringResource(it, 8)
            } else {
                stringResource(it)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.windowBackground
    ) {
        Box(Modifier.fillMaxSize()) {
            IconButton(
                onClick = cfg.onBack,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopStart)
                    .padding(start = 22.dp, top = 16.dp)
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.action_back),
                    tint = colors.textPrimary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = (16.dp + 40.dp + 8.dp))
            ) {
                Text(
                    text = stringResource(cfg.titleRes),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 6.dp, bottom = 12.dp)
                )

                cfg.descriptionRes?.let {
                    Text(
                        text = stringResource(it),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = colors.textPrimary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(start = 6.dp, bottom = 22.dp)
                    )
                }

                cfg.fields.forEach { f ->
                    ComponentTextInput(
                        hintRes = f.labelRes,
                        value = values[f.key].orEmpty(),
                        onValueChange = { v -> values = values.toMutableMap().also { it[f.key] = v } },
                        kind = f.kind,
                        isError = errors[f.key] != null,
                        errorMessage = errors[f.key],
                        onFocusChanged = { focused ->
                            if (!focused) touchedFields[f.key] = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        attempted = true
                        if (cfg.canSubmit(values)) cfg.onSubmit(values)
                    },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.brand_400),
                        disabledContainerColor = colorResource(id = R.color.brand_400),
                        contentColor = Color.White,
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = stringResource(cfg.submitButtonRes),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                cfg.textButtonRes?.let { btnRes ->
                    TextButton(
                        onClick = cfg.onTextButton,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(btnRes),
                            color = colorResource(id = R.color.purple400_purple500),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ComponentTextInput(
    @StringRes hintRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    kind: FieldKind,
    isError: Boolean,
    errorMessage: String?,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val onTextChangedColor = if (value.isNotBlank())
        colorResource(id = R.color.purple400_purple500)
    else
        colorResource(id = R.color.gray200_gray400)

    val activeNotFilledColor = colorResource(id = R.color.purple400_purple500)
    val filledNotActiveColor = colorResource(id = R.color.gray_400)
    val labelColor = colorResource(id = R.color.gray200_gray400)
    val textColorArgb = HabiticaTheme.colors.textPrimary.toArgb()

    AndroidView(
        factory = { ctx ->
            LayoutInflater.from(ctx)
                .inflate(R.layout.component_text_input, null, false)
                .apply {
                    findViewById<TextInputLayout>(R.id.text_input_layout)
                        .setBoxBackgroundColorResource(R.color.gray600_gray10)
                }
        },
        update = { view ->
            val til = view.findViewById<TextInputLayout>(R.id.text_input_layout)
            val edit = view.findViewById<AppCompatEditText>(R.id.text_edit_text)

            til.hint = view.context.getString(hintRes)

            edit.inputType = when (kind) {
                FieldKind.EMAIL -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                FieldKind.URI -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
                FieldKind.MULTILINE -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                else ->
                    InputType.TYPE_CLASS_TEXT or
                            if (kind == FieldKind.PASSWORD) InputType.TYPE_TEXT_VARIATION_PASSWORD else 0
            }
            edit.transformationMethod =
                if (kind == FieldKind.PASSWORD) PasswordTransformationMethod.getInstance() else null

            if (kind == FieldKind.MULTILINE) {
                edit.inputType = edit.inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                edit.height = 115.dpToPx(context = view.context)
                edit.gravity = Gravity.TOP and Gravity.START
                edit.isVerticalScrollBarEnabled = true
            }

            fun syncColors(focused: Boolean) {
                val active = focused || edit.text?.isNotBlank() == true
                val filledNotActive = !focused && edit.text?.isNotBlank() == true
                val activeNotFilled = focused && edit.text?.isBlank() == true
                val strokeColor = if (active) onTextChangedColor else labelColor

                til.defaultHintTextColor = ColorStateList.valueOf(strokeColor.toArgb())
                til.setBoxStrokeColorStateList(ColorStateList.valueOf(strokeColor.toArgb()))

                if (activeNotFilled) {
                    til.defaultHintTextColor = ColorStateList.valueOf(activeNotFilledColor.toArgb())
                    til.setBoxStrokeColorStateList(ColorStateList.valueOf(activeNotFilledColor.toArgb()))
                }
                if (filledNotActive) {
                    til.defaultHintTextColor = ColorStateList.valueOf(filledNotActiveColor.toArgb())
                    til.setBoxStrokeColorStateList(ColorStateList.valueOf(filledNotActiveColor.toArgb()))
                }
            }

            syncColors(edit.isFocused)

            edit.setOnFocusChangeListener { _, focused ->
                syncColors(focused)
                onFocusChanged(focused)
            }

            edit.doAfterTextChanged {
                syncColors(edit.isFocused)
                onValueChange(it.toString())
            }

            if (edit.text.toString() != value) {
                edit.setText(value)
                edit.setSelection(value.length)
            }

            til.isErrorEnabled = isError
            til.error = errorMessage
            til.setBoxStrokeWidth(2)
            edit.setTextColor(textColorArgb)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

enum class FieldKind { TEXT, PASSWORD, MULTILINE, URI, EMAIL }

data class FieldConfig(
    val key: String,
    @StringRes val labelRes: Int,
    val kind: FieldKind = FieldKind.TEXT,
    val initialValue: String = "",
    val validator: (String) -> Int? = { null }
)

data class FormScreenConfig(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int? = null,
    val fields: List<FieldConfig>,
    @StringRes val submitButtonRes: Int,
    val onSubmit: (Map<String, String>) -> Unit,
    val canSubmit: (Map<String, String>) -> Boolean,
    @StringRes val textButtonRes: Int? = null,
    val onTextButton: () -> Unit = {},
    val onBack: () -> Unit
)

@Composable
fun ChangeUsernameScreen(
    initial: String,
    onBack: () -> Unit,
    onSave: (newUsername: String) -> Unit
) {
    val fields = listOf(
        FieldConfig(
            key = "username",
            labelRes = R.string.username,
            kind = FieldKind.TEXT,
            initialValue = initial,
            validator = {
                when {
                    it.isBlank() -> R.string.username_requirements
                    it.length > 20 -> R.string.username_requirements
                    !Regex("^[A-Za-z0-9_-]+$").matches(it) -> R.string.username_requirements
                    else -> null
                }
            }
        )
    )

    ConfigurableFormScreen(
        FormScreenConfig(
            titleRes = R.string.change_username,
            descriptionRes = R.string.change_username_description,
            fields = fields,
            submitButtonRes = R.string.change_username,
            canSubmit = { vals -> fields.all { f -> f.validator(vals[f.key].orEmpty()) == null } },
            onSubmit = { vals -> onSave(vals["username"]!!.trim()) },
            onBack = onBack
        )
    )
}

@Composable
fun ChangeEmailScreen(
    initialEmail: String,
    onBack: () -> Unit,
    onSave: (newEmail: String, password: String) -> Unit,
    onForgotPassword: () -> Unit
) {
    val fields = listOf(
        FieldConfig(
            key = "email",
            labelRes = R.string.email,
            kind = FieldKind.EMAIL,
            initialValue = initialEmail,
            validator = { if (it.isBlank()) R.string.email_invalid else null }
        ),
        FieldConfig(
            key = "password",
            labelRes = R.string.password,
            kind = FieldKind.PASSWORD,
            validator = { if (it.length < 8) R.string.password_too_short else null }
        )
    )

    ConfigurableFormScreen(
        FormScreenConfig(
            titleRes = R.string.change_email,
            descriptionRes = R.string.change_email_description,
            fields = fields,
            submitButtonRes = R.string.change_email,
            canSubmit = { vals -> fields.all { f -> f.validator(vals[f.key].orEmpty()) == null } },
            onSubmit = { vals -> onSave(vals["email"]!!, vals["password"]!!) },
            textButtonRes = R.string.forgot_pw_btn,
            onTextButton = onForgotPassword,
            onBack = onBack
        )
    )
}

@Composable
fun ChangeDisplayNameScreen(
    initial: String,
    onBack: () -> Unit,
    onSave: (newDisplayName: String) -> Unit
) {
    val fields = listOf(
        FieldConfig(
            key = "displayName",
            labelRes = R.string.display_name,
            kind = FieldKind.TEXT,
            validator = { if (it.isBlank()) R.string.display_name_length_error else null },
            initialValue = initial
        )
    )

    ConfigurableFormScreen(
        FormScreenConfig(
            titleRes = R.string.change_display_name,
            descriptionRes = R.string.display_name_description,
            fields = fields,
            submitButtonRes = R.string.change_display_name,
            canSubmit = { vals -> vals["displayName"]!!.isNotBlank() },
            onSubmit = { vals -> onSave(vals["displayName"]!!) },
            onBack = onBack
        )
    )
}

@Composable
fun AboutMeScreen(
    initial: String,
    onBack: () -> Unit,
    onSave: (aboutText: String) -> Unit
) {
    val fields = listOf(
        FieldConfig(
            key          = "about",
            labelRes     = R.string.about_me,
            kind         = FieldKind.MULTILINE,
            initialValue = initial
        )
    )

    ConfigurableFormScreen(
        FormScreenConfig(
            titleRes        = R.string.about_me,
            descriptionRes  = R.string.about_me_description,
            fields          = fields,
            submitButtonRes = R.string.save_about_me,
            canSubmit       = { true },
            onSubmit        = { vals -> onSave(vals["about"]!!.trim()) },
            onBack          = onBack
        )
    )
}

@Composable
fun PhotoUrlScreen(
    initial: String,
    onBack: () -> Unit,
    onSave: (photoUrl: String) -> Unit
) {
    val fields = listOf(
        FieldConfig(
            key = "photoUrl",
            labelRes = R.string.photo_url,
            kind = FieldKind.URI,
            initialValue = initial
        )
    )

    ConfigurableFormScreen(
        FormScreenConfig(
            titleRes = R.string.photo_url,
            descriptionRes = R.string.photo_url_description,
            fields = fields,
            submitButtonRes = R.string.save_photo_url,
            canSubmit = { vals -> vals["photoUrl"]!!.isNotBlank() },
            onSubmit = { vals -> onSave(vals["photoUrl"]!!.trim()) },
            onBack = onBack
        )
    )
}

@Preview(
    name = "ChangeUsername – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewChangeUsernameScreenDark() {
    HabiticaTheme {
        ChangeUsernameScreen(
            initial = "",
            onBack = {},
            onSave = { }
        )
    }
}

@Preview(
    name = "ChangeEmail – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewChangeEmailScreenDark() {
    HabiticaTheme {
        ChangeEmailScreen(
            onBack = {},
            initialEmail = "",
            onSave = { newEmail, password -> },
            onForgotPassword = {}
        )
    }
}

@Preview(
    name = "ChangeDisplayName – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewChangeDisplayNameScreenDark() {
    HabiticaTheme {
        ChangeDisplayNameScreen(
            initial = "displayName",
            onBack = {},
            onSave = { }
        )
    }
}

@Preview(
    name = "AboutMe – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewAboutMeScreenDark() {
    HabiticaTheme {
        AboutMeScreen(
            initial = "",
            onBack = {},
            onSave = { }
        )
    }
}

@Preview(
    name = "PhotoURL – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewPhotoUrlScreenDark() {
    HabiticaTheme {
        PhotoUrlScreen(
            initial = "",
            onBack = {},
            onSave = { }
        )
    }
}


@Preview(
    name = "ChangePasswordScreen – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewChangePasswordScreenDark() {
    HabiticaTheme {
        ChangePasswordScreen(
            onBack = {},
            onSave = { old, new -> },
            onForgotPassword = {}
        )
    }
}
