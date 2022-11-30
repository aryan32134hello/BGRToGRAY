package com.example.bgrtogray

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    //private final val GALLERYREQCODE:Int = 1000
    lateinit var iv1:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(this))
        }
        val chose:Button = findViewById(R.id.choose)
        val cnvrt:Button = findViewById(R.id.convert)
        iv1 = findViewById(R.id.imageView1)
        val iv2:ImageView = findViewById(R.id.imageView2)

        chose.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val iGallery:Intent = Intent(Intent.ACTION_PICK)
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                getResult.launch(iGallery)
            }
        })
        cnvrt.setOnClickListener(){
            val drawable: Drawable = iv1.drawable

            val bitmap: Bitmap = getBitmapFromDrawable(drawable)
            var imageString = getStringImage(bitmap)
            val py = Python.getInstance()
            val pyo: PyObject = py.getModule("script")
            val obj: PyObject = pyo.callAttr("main", imageString)
            val str: String = obj.toString()
            val data = android.util.Base64.decode(str, android.util.Base64.DEFAULT)
            val bmp: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            iv2.setImageBitmap(bmp)
        }

    }
    private fun getStringImage(bitmap: Bitmap): String {
        val baos: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos)
        val imageByte = baos.toByteArray()
        val encodedImage:String = android.util.Base64.encodeToString(imageByte, android.util.Base64.DEFAULT)
        return encodedImage
    }
    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }
        private val getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    iv1.setImageURI(it.data?.data)
                }
            }
    }
