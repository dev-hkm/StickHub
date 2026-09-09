package com.hkm.stickhub.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.CategoryValidator
import com.hkm.stickhub.ui.i18n.LocalStickHubStrings
import com.hkm.stickhub.ui.i18n.text

@Composable
fun AddCategoryDialog(
    categories: List<CategoryItem> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val strings = LocalStickHubStrings.current
    var categoryName by remember { mutableStateOf("") }
    val validation = remember(categoryName, categories) {
        CategoryValidator.validate(categoryName, categories)
    }
    val isError = validation is CategoryValidator.Result.Error && categoryName.isNotBlank()
    val errorMessage = (validation as? CategoryValidator.Result.Error)?.message

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.text("New Category")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { if (it.length <= CategoryValidator.MAX_LENGTH) categoryName = it },
                    label = { Text(strings.text("Category Name")) },
                    placeholder = { Text(if (strings.locale == com.hkm.stickhub.ui.i18n.AppLanguage.VIETNAMESE) "ví dụ: Game, Anime, Thú cưng..." else "e.g. Gaming, Anime, Pets...") },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError && errorMessage != null) {
                            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                text = "${categoryName.trim().length}/${CategoryValidator.MAX_LENGTH}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validation is CategoryValidator.Result.Valid) {
                        onConfirm(categoryName.trim())
                        onDismiss()
                    }
                },
                enabled = validation is CategoryValidator.Result.Valid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.text("Create"))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.text("Cancel"))
            }
        }
    )
}
