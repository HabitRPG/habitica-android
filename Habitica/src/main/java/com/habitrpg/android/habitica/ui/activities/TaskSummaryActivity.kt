package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asLiveData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.TaskDescriptionBuilder
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.CompletedAt
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.UserRow
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TaskSummaryViewModel @Inject constructor(
    userRepository : UserRepository,
    userViewModel : MainUserViewModel,
    val taskRepository : TaskRepository,
    val socialRepository : SocialRepository
) : BaseViewModel(userRepository, userViewModel) {
    val taskID : String = ""

    val task = taskRepository.getTask(taskID).asLiveData()

    fun getMember(userID : String?) : Flow<Member?> {
        return socialRepository.getMember(userID)
    }
}

@AndroidEntryPoint
class TaskSummaryActivity : BaseActivity() {
    override fun getLayoutResId() : Int? = null

    private val viewModel : TaskSummaryViewModel by viewModels()

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabiticaTheme {
                TaskSummaryView(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TaskSummaryView(viewModel : TaskSummaryViewModel) {
    val taskDescriptionBuilder = TaskDescriptionBuilder(LocalContext.current)
    val task by viewModel.task.observeAsState()
    val titleModifier = Modifier.padding(top = 30.dp)
    val textModifier = Modifier.padding(top = 4.dp)
    val activity = LocalContext.current as? Activity

    if (task != null) {
        val darkestColor = HabiticaTheme.colors.textPrimaryFor(task)
        val topTextColor = if ((task?.value ?: 0.0) >= -20) colorResource(
            task?.extraDarkTaskColor ?: R.color.white
        ) else Color.White
        val systemUiController = rememberSystemUiController()
        val statusBarColor = HabiticaTheme.colors.primaryBackgroundFor(task)
        val lightestColor = HabiticaTheme.colors.contentBackgroundFor(task)
        DisposableEffect(systemUiController) {
            systemUiController.setStatusBarColor(statusBarColor, darkIcons = true)
            systemUiController.setNavigationBarColor(lightestColor)
            onDispose {}
        }
        Column(Modifier.background(statusBarColor)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Button(
                    onClick = {
                        if (activity != null) {
                            activity.finish()
                            return@Button
                        }
                        MainNavigationController.navigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = darkestColor),
                    elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                ) {
                    Image(
                        painterResource(R.drawable.arrow_back),
                        stringResource(R.string.action_back),
                        colorFilter = ColorFilter.tint(
                            topTextColor
                        )
                    )
                }
                Text(
                    stringResource(R.string.task_summary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = topTextColor,
                    modifier = Modifier.padding(start = 0.dp)
                )
            }
            Column(
                Modifier
                    .shadow(16.dp)
                    .background(
                        lightestColor,
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .padding(20.dp, 5.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Text(
                    stringResource(R.string.title),
                    fontSize = 16.sp,
                    color = darkestColor,
                    fontWeight = FontWeight.Medium,
                    modifier = titleModifier
                )
                Text(
                    task?.text ?: "", fontSize = 16.sp, color = darkestColor,
                    fontWeight = FontWeight.Normal, modifier = textModifier
                )
                if (task?.notes?.isNotBlank() == true) {
                    Text(
                        stringResource(R.string.notes),
                        fontSize = 16.sp,
                        color = darkestColor,
                        fontWeight = FontWeight.Medium,
                        modifier = titleModifier
                    )
                    Text(
                        task?.notes ?: "",
                        fontSize = 16.sp,
                        color = darkestColor,
                        fontWeight = FontWeight.Normal,
                        modifier = textModifier
                    )
                }
                if (task?.type != TaskType.REWARD) {
                    Text(
                        stringResource(R.string.description),
                        fontSize = 16.sp,
                        color = darkestColor,
                        fontWeight = FontWeight.Medium,
                        modifier = titleModifier
                    )
                    Text(
                        task?.let { taskDescriptionBuilder.describe(it) }!!.makeBoldComposable(),
                        fontSize = 16.sp,
                        color = darkestColor,
                        fontWeight = FontWeight.Normal,
                        modifier = textModifier
                    )
                }
                if (task?.type == TaskType.REWARD) {
                    Text(
                        stringResource(R.string.cost),
                        fontSize = 16.sp,
                        color = darkestColor,
                        fontWeight = FontWeight.Medium,
                        modifier = titleModifier.padding(bottom = 4.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .background(
                                HabiticaTheme.colors.windowBackgroundFor(task),
                                MaterialTheme.shapes.medium
                            )
                            .padding(15.dp)
                            .fillMaxWidth()
                    ) {
                        Image(HabiticaIconsHelper.imageOfGold().asImageBitmap(), null)
                        Text(
                            "${task?.value}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = darkestColor
                        )
                    }
                }
                if (task?.checklist?.isNotEmpty() == true) {
                    task?.checklist?.let { checklist ->
                        Text(
                            stringResource(R.string.checklist),
                            fontSize = 16.sp,
                            color = darkestColor,
                            fontWeight = FontWeight.Medium,
                            modifier = titleModifier.padding(bottom = 4.dp)
                        )
                        for (item in checklist) {
                            Text(
                                item.text ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = darkestColor,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .background(
                                        HabiticaTheme.colors.windowBackgroundFor(task),
                                        MaterialTheme.shapes.medium
                                    )
                                    .padding(15.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
                if (task?.group?.assignedUsersDetail?.isNotEmpty() == true) {
                    Text(
                        stringResource(R.string.assigned_to),
                        fontSize = 16.sp,
                        color = darkestColor,
                        fontWeight = FontWeight.Medium,
                        modifier = titleModifier.padding(bottom = 4.dp)
                    )
                    for (item in task?.group?.assignedUsersDetail ?: emptyList()) {
                        val member = viewModel.getMember(item.assignedUserID).collectAsState(null)
                        UserRow(
                            item.assignedUsername ?: "", member.value,
                            Modifier
                                .padding(vertical = 4.dp)
                                .background(
                                    HabiticaTheme.colors.windowBackgroundFor(task),
                                    MaterialTheme.shapes.medium
                                )
                                .padding(15.dp, 12.dp)
                                .heightIn(min = 24.dp)
                                .fillMaxWidth(),
                            color = darkestColor,
                            extraContent = if (item.completed) (
                                {
                                    CompletedAt(item.completedDate)
                                }
                                ) else null
                        )
                    }
                    task?.group?.assignedUsersDetail?.find { it.assignedUserID == viewModel.userViewModel.userID }
                        ?.let {
                            Text("")
                        }
                    task?.group?.assignedDetailsFor(viewModel.userViewModel.userID)?.let {
                        val formatter = DateFormat.getDateInstance(DateFormat.SHORT)
                        Text(
                            stringResource(
                                R.string.assigned_to_you_by,
                                it.assigningUsername ?: "",
                                formatter.format(it.assignedDate ?: Date())
                            ),
                            fontSize = 14.sp,
                            color = HabiticaTheme.colors.textSecondaryFor(task),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun String.makeBoldComposable() : AnnotatedString {
    return buildAnnotatedString {
        var isBold = false
        for (segment in split("**")) {
            withStyle(style = SpanStyle(fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal)) {
                append(segment)
            }
            isBold = !isBold
        }
    }
}
