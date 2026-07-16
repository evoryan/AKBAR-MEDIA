import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.runBlocking
import android.util.Log

// We can't easily run android code from CLI like this without emulator. Let's just modify the API client to use `Any` or `ResponseBody`.
