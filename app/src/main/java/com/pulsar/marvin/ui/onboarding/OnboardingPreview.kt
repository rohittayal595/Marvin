package com.pulsar.marvin.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pulsar.marvin.ui.theme.MarvinTheme

@Preview(showBackground = true, device = "id:pixel_7_pro")
@Composable
fun Step1Preview() {
    MarvinTheme {
        Step1Height(
            heightText = "170",
            useFeetAndInches = false,
            onHeightChange = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_7_pro")
@Composable
fun Step2Preview() {
    MarvinTheme {
        Step2Weight(
            weightText = "80",
            onWeightChange = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_7_pro")
@Composable
fun Step3Preview() {
    MarvinTheme {
        Step3Target(
            targetWeightText = "72.0",
            onTargetWeightChange = {},
            minWeight = 53.5f,
            maxWeight = 72.0f,
            onStartJourney = {},
        )
    }
}
