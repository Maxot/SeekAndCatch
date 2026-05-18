package com.maxot.seekandcatch.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import com.maxot.seekandcatch.core.designsystem.generated.resources.Res
import com.maxot.seekandcatch.core.designsystem.generated.resources.baseline_music_note_24
import com.maxot.seekandcatch.core.designsystem.generated.resources.baseline_vibration_24
import com.maxot.seekandcatch.core.designsystem.generated.resources.baseline_volume_up_24
import com.maxot.seekandcatch.core.designsystem.generated.resources.ic_account
import com.maxot.seekandcatch.core.designsystem.generated.resources.ic_game
import com.maxot.seekandcatch.core.designsystem.generated.resources.ic_heart_empty
import com.maxot.seekandcatch.core.designsystem.generated.resources.ic_heart_full
import com.maxot.seekandcatch.core.designsystem.generated.resources.ic_leaderboard
import com.maxot.seekandcatch.core.designsystem.generated.resources.ic_settings
import org.jetbrains.compose.resources.DrawableResource

object SaCIcons {
    val Leaderboard: DrawableResource = Res.drawable.ic_leaderboard
    val Play: DrawableResource = Res.drawable.ic_game
    val Settings: DrawableResource = Res.drawable.ic_settings
    val Account: DrawableResource = Res.drawable.ic_account
    val Favorite: DrawableResource = Res.drawable.ic_heart_full
    val FavoriteEmpty: DrawableResource = Res.drawable.ic_heart_empty

    val Sounds: DrawableResource = Res.drawable.baseline_volume_up_24
    val Music: DrawableResource = Res.drawable.baseline_music_note_24
    val Vibration: DrawableResource = Res.drawable.baseline_vibration_24

    val Edit = Icons.Rounded.Edit
    val Done = Icons.Rounded.Done
}
