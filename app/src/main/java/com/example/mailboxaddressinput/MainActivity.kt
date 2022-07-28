package com.example.mailboxaddressinput

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<EmailAddressInputLayout>(R.id.input_1).apply {
            setTitle("发件人：")
            onAccountChangeListener = EmailAddressInputLayout.OnAccountChangeListener { isRemove, address, addressList ->
                showToast(if (isRemove) "删除了邮箱：" + address?.address else "添加了邮箱：" + address?.address)
            }
        }
        findViewById<EmailAddressInputLayout>(R.id.input_2).apply {
            setTitle("收件人：")
            onAccountChangeListener = EmailAddressInputLayout.OnAccountChangeListener { isRemove, address, addressList ->
                showToast(if (isRemove) "删除了邮箱：" + address?.address else "添加了邮箱：" + address?.address)
            }
        }
        findViewById<EmailAddressInputLayout>(R.id.input_3).apply {
            setTitle("抄送人：")
            onAccountChangeListener = EmailAddressInputLayout.OnAccountChangeListener { isRemove, address, addressList ->
                showToast(if (isRemove) "删除了邮箱：" + address?.address else "添加了邮箱：" + address?.address)
            }
        }
    }

    private fun showToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }
}