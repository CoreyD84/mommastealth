package com.airnettie.mobile.child

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.airnettie.mobile.safescope.SafeScope
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.StandardCharsets

class SafeScopeVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var running = false
    private var vpnThread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("SafeScopeVpnService", "✅ SafeScope VPN service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        Log.i("SafeScopeVpnService", "🛑 SafeScope VPN service destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (running) return

        val builder = Builder()
        builder.setSession("SafeScopeVPN")
            .addAddress("10.0.0.2", 32) // virtual interface
            .addDnsServer("8.8.8.8")    // upstream DNS
            .addRoute("0.0.0.0", 0)     // route all traffic

        vpnInterface = builder.establish()
        running = true

        vpnThread = Thread {
            try {
                val input = FileInputStream(vpnInterface!!.fileDescriptor)
                val channel = DatagramChannel.open()
                channel.connect(InetSocketAddress("8.8.8.8", 53)) // DNS relay
                val buffer = ByteBuffer.allocate(32767)

                while (running) {
                    val length = input.read(buffer.array())
                    if (length > 0) {
                        buffer.limit(length)

                        // 🔧 Inspect packet (DNS/HTTP parsing inside SafeScope)
                        // Use a duplicate buffer for decoding to preserve the original for forwarding
                        val packetString = StandardCharsets.UTF_8.decode(buffer.duplicate()).toString()
                        val blocked = SafeScope.checkAndBlock(applicationContext, packetString)

                        if (blocked) {
                            Log.w("SafeScopeVpnService", "❌ Unsafe domain blocked")
                            buffer.clear()
                            continue
                        }

                        // Forward original packet if safe
                        channel.write(buffer)
                        buffer.clear()
                    }
                }
            } catch (e: Exception) {
                Log.e("SafeScopeVpnService", "Error in VPN loop: ${e.message}", e)
            }
        }
        vpnThread?.start()

        Log.i("SafeScopeVpnService", "🟢 SafeScope VPN started")
    }

    private fun stopVpn() {
        running = false
        vpnThread?.interrupt()
        vpnThread = null
        vpnInterface?.close()
        vpnInterface = null
        Log.i("SafeScopeVpnService", "🔴 SafeScope VPN stopped")
    }
}