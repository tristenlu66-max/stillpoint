package dev.tristen.tunnel

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import android.text.format.DateUtils
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

private val Ink = Color(0xFF173747); private val Moss = Color(0xFF2A9CBF)
private val Paper = Color(0xFFF5FAFB); private val Mist = Color(0xFFE1F2F6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FocusStore.get(this).active) FocusGuardService.start(this)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 4)
        setContent { ShouYiApp(this) }
    }
}

@Composable private fun ShouYiApp(context: Context) {
    var session by remember { mutableStateOf(FocusStore.get(context)) }
    var choosing by remember { mutableStateOf(false) }
    MaterialTheme(colorScheme = lightColorScheme(primary = Moss, onPrimary = Color.White, surface = Paper, onSurface = Ink)) {
        Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
            if (choosing) AppPicker(context, session.allowed, { allowed -> FocusStore.updateAllowed(context, allowed); session=FocusStore.get(context); choosing=false }, { choosing=false })
            else Home(context, session, { session=FocusStore.get(context) }, { choosing=true })
        }
    }
}

@Composable private fun Home(context: Context, session: FocusSession, refresh: () -> Unit, choose: () -> Unit) {
    var task by remember(session.active) { mutableStateOf(session.task) }; var minutes by remember { mutableIntStateOf(50) }
    val access = isAccessibilityEnabled(context); val batteryOk = isIgnoringBatteryOptimizations(context); val connected = FocusAccessibilityService.isActuallyConnected()
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(30.dp)); Text("守一", fontSize=34.sp, fontWeight=FontWeight.Bold, color=Ink)
        Text("守住此刻，只做一事。", fontSize=15.sp, color=Moss); Spacer(Modifier.height(28.dp))
        if (session.active) ActiveCard(session, context, refresh) else {
            Text("这一段时间，你要做什么？", fontSize=18.sp, color=Ink, fontWeight=FontWeight.SemiBold); Spacer(Modifier.height(10.dp))
            OutlinedTextField(task, { task=it }, placeholder={Text("例如：写作业、阅读、做项目")}, singleLine=true, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(18.dp))
            Spacer(Modifier.height(22.dp)); Text("留出多久？", fontSize=18.sp, color=Ink, fontWeight=FontWeight.SemiBold); Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { listOf(25,50,90).forEach { value -> FilterChip(selected=minutes==value,onClick={minutes=value},label={Text("$value 分钟")}) } }
            Spacer(Modifier.height(22.dp)); Text("允许使用", fontSize=18.sp, color=Ink, fontWeight=FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp)); Text(if(session.allowed.isEmpty()) "还没有选择应用" else "已选择 ${session.allowed.size} 个应用", color=Moss)
            TextButton(onClick=choose, contentPadding=PaddingValues(0.dp)) { Text("搜索并选择应用  ›", fontSize=16.sp) }
            Spacer(Modifier.weight(1f)); Button(onClick={FocusStore.start(context,task,minutes,session.allowed); refresh()}, enabled=session.allowed.isNotEmpty() && access, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(18.dp)) { Text("开始守一", fontSize=17.sp) }
            Spacer(Modifier.height(16.dp))
        }
        StatusRow("无障碍拦截", when { !access -> "需要开启"; connected -> "双通道守护已连接"; else -> "连接失活，点此重新开启" }, access && connected) { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        StatusRow("后台守护", if(batteryOk) "已允许后台运行" else "建议关闭电池优化", batteryOk) { requestBatteryExemption(context) }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable private fun ActiveCard(session: FocusSession, context: Context, refresh: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Mist).padding(24.dp)) {
        Text("正在守一", fontSize=14.sp, color=Moss, fontWeight=FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(session.task, fontSize=28.sp, fontWeight=FontWeight.SemiBold, color=Ink)
        Text("${DateUtils.getRelativeTimeSpanString(session.endAt,System.currentTimeMillis(),DateUtils.MINUTE_IN_MILLIS)}结束", color=Moss); Spacer(Modifier.height(22.dp))
        OutlinedButton(onClick={FocusStore.stop(context); refresh()}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(16.dp)) { Text("结束本次专注") }
    }
}

@Composable private fun StatusRow(title:String, subtitle:String, good:Boolean, click:()->Unit) { Row(Modifier.fillMaxWidth().clickable(onClick=click).padding(vertical=10.dp), verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(9.dp).clip(RoundedCornerShape(9.dp)).background(if(good) Moss else Color(0xFFB7773D))); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Medium);Text(subtitle,fontSize=13.sp,color=Moss)};Text("设置 ›",color=Moss,fontSize=13.sp) } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AppPicker(context: Context, initiallySelected:Set<String>, done:(Set<String>)->Unit, back:()->Unit) {
    val apps=remember{installedLaunchableApps(context)}; var selected by remember{mutableStateOf(initiallySelected)}; var query by remember{mutableStateOf("")}; val listState=rememberLazyListState(); val scope=rememberCoroutineScope()
    val filtered=remember(apps,query){ if(query.isBlank()) apps else apps.filter{it.label.contains(query,true)||it.packageName.contains(query,true)} }
    val letters=remember(filtered){ filtered.mapNotNull{appInitial(it.label)}.distinct().sorted() }
    Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(horizontal=20.dp)) { Spacer(Modifier.height(18.dp)); Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=back){Icon(Icons.AutoMirrored.Filled.ArrowBack,"返回")};Text("选择允许的应用",fontSize=22.sp,fontWeight=FontWeight.SemiBold);Spacer(Modifier.weight(1f));TextButton(onClick={done(selected)}){Text("完成 (${selected.size})")}}; OutlinedTextField(query,{query=it},leadingIcon={Icon(Icons.Filled.Search,"搜索")},placeholder={Text("搜索应用名称或包名")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp));Spacer(Modifier.height(8.dp)); Box(Modifier.weight(1f)){ LazyColumn(state=listState,contentPadding=PaddingValues(end=28.dp)){itemsIndexed(filtered,key={_,app->app.packageName}){index,app->AppRow(app,app.packageName in selected){selected=if(it)selected+app.packageName else selected-app.packageName}; if(index<filtered.lastIndex)HorizontalDivider(color=Mist)} }; AlphabetRail(letters,filtered,listState){letter->scope.launch{val index=filtered.indexOfFirst{appInitial(it.label)==letter};if(index>=0)listState.scrollToItem(index)}} } } }
}
@Composable private fun AppRow(app:LaunchableApp,checked:Boolean,onChange:(Boolean)->Unit){ Row(Modifier.fillMaxWidth().clickable{onChange(!checked)}.padding(vertical=11.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Mist),contentAlignment=Alignment.Center){Text(app.label.take(1),color=Moss,fontWeight=FontWeight.Bold)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(app.label,fontWeight=FontWeight.Medium);Text(app.packageName,fontSize=11.sp,color=Moss,maxLines=1)};Checkbox(checked,onChange)} }
@Composable private fun AlphabetRail(letters:List<Char>,apps:List<LaunchableApp>,state:LazyListState,jump:(Char)->Unit){ Column(Modifier.fillMaxHeight().width(28.dp).padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceEvenly){letters.forEach{letter->Text(letter.toString(),fontSize=11.sp,color=Moss,modifier=Modifier.clickable{jump(letter)})}} }
private data class LaunchableApp(val label:String,val packageName:String)
private fun installedLaunchableApps(context:Context):List<LaunchableApp>{val intent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);return context.packageManager.queryIntentActivities(intent,0).asSequence().map{it.activityInfo.applicationInfo}.filter{it.packageName!=context.packageName}.distinctBy{it.packageName}.map{LaunchableApp(context.packageManager.getApplicationLabel(it).toString(),it.packageName)}.sortedWith(compareBy(Collator.getInstance(Locale.CHINA)){it.label}).toList()}
private fun appInitial(label:String):Char? { val latin = android.icu.text.Transliterator.getInstance("Han-Latin; Latin-ASCII").transliterate(label).trim(); return latin.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: '#' }
private fun isAccessibilityEnabled(context:Context):Boolean{val manager=context.getSystemService(AccessibilityManager::class.java);val mine=ComponentName(context,FocusAccessibilityService::class.java);return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any{ComponentName(it.resolveInfo.serviceInfo.packageName,it.resolveInfo.serviceInfo.name)==mine}}
private fun isIgnoringBatteryOptimizations(context:Context)=context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)
private fun requestBatteryExemption(context:Context){context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}")))}
