package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.TaskDescriptionBuilder
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.views.CompletedAt
import com.habitrpg.android.habitica.ui.views.UserRow
import com.habitrpg.shared.habitica.models.tasks.TaskType
import javax.inject.Inject

class TaskSummaryViewModel(val taskId: String) : BaseViewModel() {
    @Inject
    lateinit var taskRespository: TaskRepository
    val task = taskRespository.getTask(taskId).asLiveData()

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val taskID: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskSummaryViewModel(taskID) as T
        }
    }
}

class TaskSummaryActivity : BaseActivity() {
    override fun getLayoutResId(): Int? = null

    private val viewModel: TaskSummaryViewModel by viewModels {
        TaskSummaryViewModel.Factory(
            intent.extras?.getString(
                TaskFormActivity.TASK_ID_KEY
            ) ?: ""
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabiticaTheme {
                TaskSummaryView(viewModel = viewModel)
            }
        }
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }
}

@Composable
fun TaskSummaryView(viewModel: TaskSummaryViewModel) {
    val taskDescriptionBuilder = TaskDescriptionBuilder(LocalContext.current)
    val task by viewModel.task.observeAsState()
    val titleModifier = Modifier.padding(top = 30.dp)
    val textModifier = Modifier.padding(top = 4.dp)
    if (task != null) {
        val darkestColor = colorResource(task?.darkestTaskColor ?: R.color.text_primary)
        val systemUiController = rememberSystemUiController()
        val statusBarColor = colorResource(task?.lightTaskColor ?: R.color.brand_400)
        val lightestColor = colorResource(task?.lightestTaskColor ?: R.color.window_background)
        DisposableEffect(systemUiController) {
            systemUiController.setStatusBarColor(statusBarColor, darkIcons = true)
            systemUiController.setNavigationBarColor(lightestColor)
            onDispose {}
        }
        Column(Modifier.background(colorResource(task?.lightTaskColor ?: R.color.brand_300))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Button(
                    onClick = {
                        MainNavigationController.navigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = darkestColor),
                    elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                ) {
                    Image(
                        painterResource(R.drawable.arrow_back),
                        stringResource(R.string.action_back),
                        colorFilter = ColorFilter.tint(
                            colorResource(
                                task?.darkestTaskColor ?: R.color.white
                            )
                        )
                    )
                }
                Text(
                    stringResource(R.string.task_summary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = darkestColor,
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
                    Text(task?.let { taskDescriptionBuilder.describe(it) }!!.makeBoldComposable(),
                        fontSize = 16.sp,
                        color = darkestColor,
                        fontWeight = FontWeight.Normal,
                        modifier = textModifier)
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
                                        colorResource(
                                            task?.extraExtraLightTaskColor ?: R.color.gray_700
                                        ),
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
                        UserRow(
                            item.assignedUsername ?: "", Modifier
                                .padding(vertical = 4.dp)
                                .background(
                                    colorResource(
                                        task?.extraExtraLightTaskColor ?: R.color.gray_700
                                    ),
                                    MaterialTheme.shapes.medium
                                )
                                .padding(15.dp, 12.dp)
                                .heightIn(min = 24.dp)
                                .fillMaxWidth(),
                            color = darkestColor,
                            extraContent = if (item.completed) ({
                                CompletedAt(item.completedDate)
                            }) else null
                        )
                    }
                    task?.group?.assignedUsersDetail?.find { it.assignedUserID == viewModel.userViewModel.userID }?.let {
                        Text("", )
                    }
                }
            }
        }
    }
}

private fun String.makeBoldComposable(): AnnotatedString {
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

@Preview
@Composable
private fun TaskSummaryViewPreview() {
    TaskSummaryView(TaskSummaryViewModel(""))
}