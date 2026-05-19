package com.maxot.seekandcatch.data

import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.model.UserConfig
import com.maxot.seekandcatch.core.model.DarkThemeConfig
import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardDataSource
import com.maxot.seekandcatch.data.firebase.datasource.UserDataSource
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.FigureColor
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.ColorsRepository
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module

// ---------------------------------------------------------------------------
// Stub: AuthRepository
// ---------------------------------------------------------------------------
private class IosAuthRepository : AuthRepository {
    override suspend fun getOrCreateUser(): User = User(id = "ios-stub-user", name = "Player")
    override suspend fun getUserId(): String = "ios-stub-user"
}

// ---------------------------------------------------------------------------
// Stub: SettingsRepository
// ---------------------------------------------------------------------------
private class IosSettingsRepository : SettingsRepository {
    private val _userConfig = MutableStateFlow(
        UserConfig(darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM, isColorblindModeEnabled = false)
    )
    override val userConfig: Flow<UserConfig> = _userConfig

    private val _sound = MutableStateFlow(true)
    private val _music = MutableStateFlow(true)
    private val _vibration = MutableStateFlow(false)
    private val _difficulty = MutableStateFlow(GameDifficulty.NORMAL)
    private val _gameMode = MutableStateFlow(GameMode.FLOW)
    private val _colorblind = MutableStateFlow(false)

    override suspend fun setSoundState(newState: Boolean) { _sound.value = newState }
    override fun observeSoundState(): Flow<Boolean> = _sound

    override suspend fun setMusicState(newState: Boolean) { _music.value = newState }
    override fun observeMusicState(): Flow<Boolean> = _music

    override suspend fun setVibrationState(newState: Boolean) { _vibration.value = newState }
    override fun observeVibrationState(): Flow<Boolean> = _vibration

    override suspend fun setDifficulty(newDifficulty: GameDifficulty) { _difficulty.value = newDifficulty }
    override fun observeDifficulty(): Flow<GameDifficulty> = _difficulty

    override suspend fun setGameMode(gameMode: GameMode) { _gameMode.value = gameMode }
    override fun observeGameMode(): Flow<GameMode> = _gameMode

    override suspend fun setDarkTheme(darkTheme: Boolean) {
        _userConfig.value = _userConfig.value.copy(
            darkThemeConfig = if (darkTheme) DarkThemeConfig.DARK else DarkThemeConfig.LIGHT
        )
    }

    override suspend fun setColorblindModeEnabled(enabled: Boolean) {
        _colorblind.value = enabled
        _userConfig.value = _userConfig.value.copy(isColorblindModeEnabled = enabled)
    }

    override fun observeColorblindModeEnabled(): Flow<Boolean> = _colorblind
}

// ---------------------------------------------------------------------------
// Stub: ColorsRepository
// ---------------------------------------------------------------------------
private class IosColorsRepository : ColorsRepository {
    private val defaultColors: Set<FigureColor> = setOf(
        FigureColor.Red,
        FigureColor.Blue,
        FigureColor.Green,
        FigureColor.Yellow
    )
    private val _selectedColors = MutableStateFlow(defaultColors)

    override val selectedColors: Flow<Set<FigureColor>> = _selectedColors

    override fun getAvailableColors(): Set<FigureColor> = setOf(
        FigureColor.Red,
        FigureColor.Blue,
        FigureColor.Green,
        FigureColor.Yellow,
        FigureColor.Cyan,
        FigureColor.Magenta
    )

    override suspend fun setSelectedColors(colors: Set<FigureColor>) {
        _selectedColors.value = colors
    }

    override suspend fun getRandomSelectedColor(): FigureColor =
        _selectedColors.value.random()
}

// ---------------------------------------------------------------------------
// Stub: FiguresRepository
// ---------------------------------------------------------------------------
private class IosFiguresRepository : FiguresRepository {
    override fun getRandomFigure(id: Int): Figure = Figure.getRandomFigure(id)

    override fun getRandomFigures(itemsCount: Int): List<Figure> =
        (0 until itemsCount).map { Figure.getRandomFigure(it) }

    override fun getRandomFigures(
        itemsCount: Int,
        startId: Int,
        percentageOfSuitableGoalItems: Float,
        goal: Goal<Any>
    ): List<Figure> {
        val suitableCount = (itemsCount * percentageOfSuitableGoalItems).toInt().coerceAtLeast(1)
        val unsuitableCount = itemsCount - suitableCount
        val suitable = getFigureSuitableForGoal(goal).toList()
        val unsuitable = getFigureUnsuitableForGoal(goal).toList()

        val figures = mutableListOf<Figure>()
        repeat(suitableCount) { i ->
            figures.add(suitable[i % suitable.size].copy(id = startId + i))
        }
        repeat(unsuitableCount) { i ->
            figures.add(unsuitable[i % unsuitable.size].copy(id = startId + suitableCount + i))
        }
        figures.shuffle()
        return figures
    }

    override fun getFigureSuitableForGoal(goal: Goal<Any>): Set<Figure> {
        return when (goal) {
            is Goal.Colored -> Figure.FigureType.entries.map { type ->
                Figure(type = type, color = goal.getGoal())
            }.toSet()
            is Goal.Shaped -> listOf(
                FigureColor.Red, FigureColor.Blue, FigureColor.Green, FigureColor.Yellow
            ).map { color ->
                Figure(type = goal.getGoal(), color = color)
            }.toSet()
        }
    }

    override fun getFigureUnsuitableForGoal(goal: Goal<Any>): Set<Figure> {
        return when (goal) {
            is Goal.Colored -> {
                val targetColor = goal.getGoal()
                val otherColors = listOf(
                    FigureColor.Red, FigureColor.Blue, FigureColor.Green, FigureColor.Yellow
                ).filter { it != targetColor }
                Figure.FigureType.entries.flatMap { type ->
                    otherColors.map { color -> Figure(type = type, color = color) }
                }.toSet()
            }
            is Goal.Shaped -> {
                val targetType = goal.getGoal()
                val otherTypes = Figure.FigureType.entries.filter { it != targetType }
                otherTypes.flatMap { type ->
                    listOf(FigureColor.Red, FigureColor.Blue, FigureColor.Green, FigureColor.Yellow).map { color ->
                        Figure(type = type, color = color)
                    }
                }.toSet()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Stub: GoalsRepository
// ---------------------------------------------------------------------------
private class IosGoalsRepository : GoalsRepository {
    private val availableColors = listOf(
        FigureColor.Red, FigureColor.Blue, FigureColor.Green, FigureColor.Yellow
    )

    override suspend fun getRandomGoal(): Goal<Any> {
        return if ((0..1).random() == 0) {
            @Suppress("UNCHECKED_CAST")
            Goal.Colored(availableColors.random()) as Goal<Any>
        } else {
            @Suppress("UNCHECKED_CAST")
            Goal.Shaped(Figure.FigureType.getRandomFigureType()) as Goal<Any>
        }
    }
}

// ---------------------------------------------------------------------------
// Stub: LeaderboardDataSource
// ---------------------------------------------------------------------------
private class IosLeaderboardDataSource : LeaderboardDataSource {
    private val _records = MutableStateFlow<List<LeaderboardRecord>>(emptyList())

    override fun observeRecords(): Flow<List<LeaderboardRecord>> = _records

    override fun addRecord(record: LeaderboardRecord, userId: String, onSuccessful: (String) -> Unit) {
        _records.value = _records.value + record
        onSuccessful(userId)
    }
}

// ---------------------------------------------------------------------------
// Stub: UserDataSource
// ---------------------------------------------------------------------------
private class IosUserDataSource : UserDataSource {
    private val users = mutableMapOf<String, User>()

    override suspend fun saveUser(user: User) {
        users[user.id] = user
    }

    override suspend fun getUser(userId: String): User? = users[userId]

    override fun observeUsers(): Flow<List<User>> = flowOf(users.values.toList())
}

// ---------------------------------------------------------------------------
// Stub: LeaderboardRepository
// ---------------------------------------------------------------------------
private class IosLeaderboardRepository(
    private val dataSource: LeaderboardDataSource
) : LeaderboardRepository {
    override fun observeRecords(): Flow<List<LeaderboardRecord>> = dataSource.observeRecords()

    override suspend fun addRecord(record: LeaderboardRecord) {
        dataSource.addRecord(record, record.userId ?: "") {}
    }
}

// ---------------------------------------------------------------------------
// Stub: UserRepository
// ---------------------------------------------------------------------------
private class IosUserRepository(
    private val dataSource: UserDataSource
) : UserRepository {
    override suspend fun getUser(userId: String): User? = dataSource.getUser(userId)
    override suspend fun saveUser(user: User) = dataSource.saveUser(user)
}

// ---------------------------------------------------------------------------
// Koin module
// ---------------------------------------------------------------------------
val iosDataModule = module {
    single<LeaderboardDataSource> { IosLeaderboardDataSource() }
    single<UserDataSource> { IosUserDataSource() }
    single<AuthRepository> { IosAuthRepository() }
    single<SettingsRepository> { IosSettingsRepository() }
    single<ColorsRepository> { IosColorsRepository() }
    single<FiguresRepository> { IosFiguresRepository() }
    single<GoalsRepository> { IosGoalsRepository() }
    single<LeaderboardRepository> { IosLeaderboardRepository(get()) }
    single<UserRepository> { IosUserRepository(get()) }
}
