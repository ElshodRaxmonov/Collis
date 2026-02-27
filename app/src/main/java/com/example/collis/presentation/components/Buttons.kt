package com.example.collis.presentation.components


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Reusable action button with loading state, icon, and variant support. */
@Composable
fun CollisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier.heightIn(min = 48.dp).animateContentSize(),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                ButtonContent(text = text, loading = loading, icon = icon)
            }
        }

        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.heightIn(min = 48.dp).animateContentSize(),
                enabled = enabled && !loading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                ButtonContent(text = text, loading = loading, icon = icon)
            }
        }

        ButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.heightIn(min = 48.dp).animateContentSize(),
                enabled = enabled && !loading
            ) {
                ButtonContent(text = text, loading = loading, icon = icon)
            }
        }

        ButtonVariant.Error -> {
            Button(
                onClick = onClick,
                modifier = modifier.heightIn(min = 48.dp).animateContentSize(),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                ButtonContent(text = text, loading = loading, icon = icon)
            }
        }
    }
}

/** Internal button content — adapts spinner colour to the current content colour. */
@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    icon: ImageVector?
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Loading...", style = MaterialTheme.typography.labelLarge)
    } else {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

enum class ButtonVariant { Primary, Secondary, Text, Error }

/** Floating Action Button — supports both standard (icon-only) and extended (icon + text) forms. */
@Composable
fun CollisFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    text: String? = null
) {
    if (expanded && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            icon = { Icon(imageVector = icon, contentDescription = contentDescription) },
            text = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollisButtonPreview() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CollisButton(text = "Primary", onClick = {})
        CollisButton(text = "Secondary", onClick = {}, variant = ButtonVariant.Secondary)
        CollisButton(text = "Text", onClick = {}, variant = ButtonVariant.Text)
        CollisButton(text = "Error", onClick = {}, variant = ButtonVariant.Error)
        CollisButton(text = "Loading", onClick = {}, loading = true)
        CollisButton(text = "With Icon", onClick = {}, icon = Icons.Default.Add)
        CollisButton(text = "Disabled", onClick = {}, enabled = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun CollisFABPreview() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CollisFAB(onClick = {}, icon = Icons.Default.Add, contentDescription = "Add")
        CollisFAB(onClick = {}, icon = Icons.Default.Add, contentDescription = "Add", expanded = true, text = "New Task")
    }
}