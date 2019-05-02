package `in`.eightfolds.camerasample

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.camerakit.CameraKitView
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_main.button
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {


    private var cameraKitView: CameraKitView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
//        supportActionBar?.hide()
        cameraKitView = findViewById<CameraKitView>(R.id.camera)
//        cameraKitView?.sensorPreset = CameraKit.SENSOR_PRESET_LANDSCAPE

        val permissions = arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        button.setOnClickListener {
            Permissions.check(this/*context*/, permissions, null/*options*/, null, object : PermissionHandler() {
                override fun onGranted() {
                    captureImage()

                }

            })
        }
    }

    private fun captureImage() {

        cameraKitView?.captureImage { cameraKitView, bytes ->
            val pictureFile = getOutputMediaFile() ?: return@captureImage
            try {
                val fos = FileOutputStream(pictureFile)
                fos.write(bytes)
                fos.close()
                cropImage(pictureFile.path)
                Toast.makeText(this, "Image Capture Successful", Toast.LENGTH_LONG).show()

            } catch (e: FileNotFoundException) {
                e.printStackTrace()

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    private fun cropImage(path: String) {

        val imageResolution = cameraKitView?.photoResolution

        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width = point.x
        val height = point.y


        val options = BitmapFactory.Options()
        options.inMutable = true
//        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        val bitmap = BitmapFactory.decodeFile(path)
//        val canvas = Canvas(bmp)

        val bitmapWidth = bitmap.width.toDouble()
        val bitmapheight = bitmap.height.toDouble()


        var viewWidth: Double = view_main?.width!!.toDouble()
        var viewHeight = view_main?.height!!.toDouble()


        val finalWidth = (bitmapWidth / width) * viewWidth
        val finalHeight = (bitmapheight / height) * viewHeight
        // make sure crop isn't larger than bitmap size
//        cropWidth = if (cropWidth!! > bitmapWidth) bitmapWidth else cropWidth
//        cropHeight = if (cropHeight!! > bitmapheight) bitmapheight else cropHeight


        val newX = (bitmapWidth / width) * view_main.x
        val newY = (bitmapheight / height) * view_main.y

//        val croppedImage = Bitmap.createBitmap(bitmap, view_main.x.toInt(), view_main.y.toInt(), view_main.width, view_main.height)
        val croppedImage =
            Bitmap.createBitmap(bitmap, newX.toInt(), newY.toInt(), finalWidth.toInt(), finalHeight.toInt())
//        val croppedImage =
//            Bitmap.createBitmap(bitmap, view_main.x.toInt(), view_main.y.toInt(), finalWidth.toInt(), finalHeight.toInt())

        try {
            val fileOutputStream = FileOutputStream(getOutputMediaFile())
            croppedImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onStart() {
        super.onStart()
        cameraKitView!!.onStart()
    }

    override fun onResume() {
        super.onResume()
        cameraKitView!!.onResume()
    }

    override fun onPause() {
        cameraKitView!!.onPause()
        super.onPause()
    }

    override fun onStop() {
        cameraKitView!!.onStop()
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraKitView!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(
            Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "CameraSample"
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("CameraSample", "failed to create directory")
                return null
            }
        }
        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(Date())
        val mediaFile: File
        mediaFile = File(
            mediaStorageDir.path + File.separator
                    + "IMG_" + timeStamp + Random().nextInt() + ".jpg"
        )

        return mediaFile
    }
}
