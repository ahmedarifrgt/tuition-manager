package com.example.ui.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TuitionViewModel
import com.example.ui.components.TuitionTopBar
import com.example.ui.theme.*
import com.example.util.ActionHelper

@Composable
fun AboutAuthorScreen(
    viewModel: TuitionViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    fun openUrl(url: String) {
        try {
            val fullUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            ActionHelper.shareText(context, "Contact MD ARIF", "Email: $email")
        }
    }

    Scaffold(
        topBar = {
            TuitionTopBar(
                title = "About Author",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Hero Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("author_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "MD ARIF",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Computer Science & Engineering Student",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Aspiring Software Engineer",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Southern University Bangladesh",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Contact & Social Actions Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("author_contact_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Contact & Profiles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = RedError, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Textile, Green Valley Housing Society, Chattogram",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("01879524393", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { ActionHelper.callNumber(context, "01879524393") },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = GreenSuccessContainer),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = GreenSuccess, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("01879524393"))
                                    Toast.makeText(context, "Phone number copied", Toast.LENGTH_SHORT).show()
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = SlateSurfaceVariant),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("ahmedarif.rgt@gmail.com", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { sendEmail("ahmedarif.rgt@gmail.com") },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send Email", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("ahmedarif.rgt@gmail.com"))
                                    Toast.makeText(context, "Email copied", Toast.LENGTH_SHORT).show()
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = SlateSurfaceVariant),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // LinkedIn
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = NavyLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("linkedin.com/ahmedarif.rgt", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { openUrl("linkedin.com/in/ahmedarif-rgt") },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open LinkedIn", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("linkedin.com/ahmedarif.rgt"))
                                    Toast.makeText(context, "LinkedIn link copied", Toast.LENGTH_SHORT).show()
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = SlateSurfaceVariant),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // GitHub
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = NavyDark, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("github.com/ahmedarifrgt", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { openUrl("github.com/ahmedarifrgt") },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = SlateSurfaceVariant),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open GitHub", tint = SlateTextPrimary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("github.com/ahmedarifrgt"))
                                    Toast.makeText(context, "GitHub link copied", Toast.LENGTH_SHORT).show()
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = SlateSurfaceVariant),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // About Me Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("author_about_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "About Me",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "I am a passionate and motivated Computer Science and Engineering student at Southern University Bangladesh, with a strong interest in software development, web development, mobile application development, and emerging technologies.\n\n" +
                                "I have hands-on experience with Python, Flutter, FastAPI, Java, MySQL, Git, and SQLite, and I enjoy developing practical software solutions that are reliable, user-friendly, and professionally designed. I have worked on projects including a Gym Management System, Secure Electronic Voting System, University Chatbot System, and business management applications.\n\n" +
                                "I am continuously developing my programming, problem-solving, and software engineering skills through academic projects, personal development, and practical implementation. I am particularly interested in Artificial Intelligence, Machine Learning, secure software systems, full-stack development, and mobile application development.\n\n" +
                                "My goal is to build a successful career as a Software Engineer by contributing to meaningful technology projects, continuously learning modern technologies, and developing solutions that provide real-world value.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }

            // Education Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("author_education_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Education",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // B.Sc.
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "B.Sc. in Computer Science & Engineering",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Southern University Bangladesh",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Graduation: 2026", style = MaterialTheme.typography.labelSmall)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GreenSuccessContainer
                                ) {
                                    Text(
                                        text = "CGPA: 3.3",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GreenSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // HSC
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Higher Secondary Certificate (HSC) – Science",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Langadu Government Degree College",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Board: Science", style = MaterialTheme.typography.labelSmall)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GreenSuccessContainer
                                ) {
                                    Text(
                                        text = "GPA: 4.86",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GreenSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Technical Skills Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("author_skills_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Technical Skills & Technologies",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SkillGroupRow("Programming", listOf("Python", "Java", "Dart"))
                    SkillGroupRow("Mobile", listOf("Flutter", "Android"))
                    SkillGroupRow("Backend & DB", listOf("FastAPI", "MySQL", "SQLite"))
                    SkillGroupRow("Tools", listOf("Git", "GitHub"))

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Areas of Interest",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val interests = listOf(
                        "Software Development",
                        "Web Development",
                        "Full-Stack Development",
                        "Mobile App Development",
                        "AI & Machine Learning"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        interests.take(2).forEach { item ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        interests.drop(2).forEach { item ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SkillGroupRow(category: String, skills: List<String>) {
    Column {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            skills.forEach { skill ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = skill,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
