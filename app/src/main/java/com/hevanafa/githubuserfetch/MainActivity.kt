package com.hevanafa.githubuserfetch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.hevanafa.githubuserfetch.ui.theme.GithubUserFetchTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

data class State (
    val search_term: String = "",
    val response_text: String = "",

    val usernames: ArrayList<String> = arrayListOf(),
    val images: ArrayList<String> = arrayListOf()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GithubUserFetchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FetchScaffold()
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(State())
    private val uiStateFlow = _uiState.asStateFlow()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FetchScaffold() {
        val self = this
        val state = uiStateFlow.collectAsState()
        val search_term = state.value.search_term

        Scaffold { _ ->
            Column (modifier = Modifier.padding(20.dp)) {
                Text("Github User Search", fontSize = 30.sp)

                TextField (search_term, { newValue ->
                    _uiState.update {
                        it.copy(search_term = newValue)
                    }
                }, modifier = Modifier.fillMaxWidth())

                Row (modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .clickable {
                            _uiState.update {
                                it.copy(response_text = "Fetching data...")
                            }

                            val queue = Volley.newRequestQueue(self)

                            if (search_term.isEmpty()) {
                                _uiState.update {
                                    it.copy(response_text = "Empty input box.")
                                }
                                return@clickable
                            }

                            val url = "https://api.github.com/search/users?q=$search_term"

                            val req = JsonObjectRequest(Request.Method.GET, url, null,
                                { res ->
                                    val usernames = uiStateFlow.value.usernames
                                    val images = uiStateFlow.value.images

                                    usernames.clear()
                                    images.clear()

                                    val json_ary = res.getJSONArray("items")
                                    for (a in 0 until json_ary.length()) {
                                        val item = json_ary.get(a) as JSONObject

                                        usernames.add(item.getString("login"))
                                        images.add(item.getString("avatar_url"))
                                    }

                                    _uiState.update {
                                        it.copy(response_text = "Total count: %d".format(res.getInt("total_count")))
                                    }
                                },
                                { err ->
                                    _uiState.update {
                                        it.copy(response_text = err.localizedMessage ?: "")
                                    }
                                })

                            queue.add(req)

                            // Simple request
//                    val queue = Volley.newRequestQueue(self)
//                    val url = "https://www.google.com"
//
//                    val req = StringRequest(
//                        Request.Method.GET,
//                        url,
//                        { res ->
//                            _uiState.update {
//                                it.copy(response_text = res)
//                            }
//                        },
//                        {
//                            _uiState.update {
//                                it.copy(response_text = "Unable to fetch")
//                            }
//                        }
//                    )
//
//                    queue.add(req)
                },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Search")
                }

                Row {
                    val response_text = state.value.response_text
                    Text(response_text)
                }

                LazyColumn {
                    val usernames = state.value.usernames
                    val images = state.value.images

                    this.itemsIndexed(usernames) { idx, username ->
                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .drawBehind {
                                val y = size.height - 1

                                drawLine(
                                    Color.Black,
                                    Offset(0f, y),
                                    Offset(size.width, y),
                                    1f
                                )
                            }, verticalAlignment = Alignment.CenterVertically) {

                            AsyncImage(
                                model = images[idx],
                                contentDescription = username
                            )

                            Text(username)
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GithubUserFetchTheme {
        Greeting("Android")
    }
}