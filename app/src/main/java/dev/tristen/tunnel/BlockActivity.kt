package dev.tristen.tunnel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class BlockActivity : ComponentActivity() {
    companion object { const val EXTRA_BLOCKED_PACKAGE = "blocked_package" }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = FocusStore.get(this)
        if (!session.active) { finish(); return }
        setContent { BlockScreen(session.task, ((session.endAt - System.currentTimeMillis()).coerceAtLeast(0) / 60_000L) + 1, { moveTaskToBack(true) }, { FocusStore.stop(this@BlockActivity); finish() }) }
    }
}

@Composable private fun BlockScreen(task:String, minutes:Long, back:()->Unit, stop:()->Unit) {
    val paper=Color(0xFFF5FAFB); val ink=Color(0xFF173747); val moss=Color(0xFF2A9CBF)
    MaterialTheme(colorScheme=lightColorScheme(primary=moss,onPrimary=Color.White,surface=paper,onSurface=ink)) { Surface(color=paper,modifier=Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(30.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Text("守一",color=moss,fontWeight=FontWeight.Bold,fontSize=20.sp); Spacer(Modifier.height(28.dp)); Text("此刻，不去那里。",fontSize=31.sp,fontWeight=FontWeight.SemiBold,color=ink,textAlign=TextAlign.Center); Spacer(Modifier.height(16.dp)); Text("你正在「$task」\n还剩约 $minutes 分钟",fontSize=17.sp,lineHeight=27.sp,color=moss,textAlign=TextAlign.Center); Spacer(Modifier.height(48.dp)); Button(onClick=back,modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(18.dp)){Text("回到允许使用的应用",fontSize=17.sp)}; Spacer(Modifier.height(12.dp)); TextButton(onClick=stop){Text("结束本次专注",color=moss)}
    } } }
}
