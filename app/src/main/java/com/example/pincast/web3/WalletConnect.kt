package com.example.pincast.web3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Utility class for wallet connection and Web3 interactions
 */
object WalletConnect {
    private const val TAG = "WalletConnect"
    
    // Wallet connection state
    private var walletAddress: String? = null
    private var chainId: String? = null
    
    // Check if wallet is connected
    fun isWalletConnected(): Boolean {
        return !walletAddress.isNullOrEmpty()
    }
    
    // Get connected wallet address
    fun getWalletAddress(): String? {
        return walletAddress
    }
    
    /**
     * Connect to Keplr wallet
     * This will open Keplr in mobile browser if installed
     */
    fun connectKeplrWallet(context: Context) {
        try {
            val keplrIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("keplr://wallet/")
                // Use a scheme that Keplr mobile understands
                // This is a basic implementation - in a real app, we would implement
                // a complete deep linking handshake
            }
            context.startActivity(keplrIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect Keplr wallet", e)
            // Fallback to browser - open Keplr website
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wallet.keplr.app/")
            }
            context.startActivity(browserIntent)
        }
    }
    
    /**
     * Generate NFT metadata for an image
     * @param name NFT name
     * @param description NFT description
     * @param imageCid IPFS CID of the image
     * @return JSON metadata string that conforms to NFT standards
     */
    fun generateNftMetadata(name: String, description: String, imageCid: String): String {
        val metadata = JSONObject().apply {
            put("name", name)
            put("description", description)
            put("image", "ipfs://$imageCid")
            // Add additional properties that are common for Cosmos NFTs
            put("attributes", JSONObject().apply {
                put("created_with", "PinCast")
                put("timestamp", System.currentTimeMillis().toString())
            })
        }
        
        return metadata.toString(2)
    }
    
    /**
     * Generate a Stargaze mint link for the given collection CID
     * This creates a direct link to mint an NFT on Stargaze
     */
    fun generateStargazeMintLink(collectionCid: String): String {
        return "https://app.stargaze.zone/launchpad/create?collection=$collectionCid"
    }
    
    /**
     * Manually set wallet address (to be used after successful connection)
     */
    fun setWalletConnection(address: String, chain: String) {
        walletAddress = address
        chainId = chain
        Log.d(TAG, "Wallet connected: $address on chain $chain")
    }
    
    /**
     * Disconnect wallet
     */
    fun disconnectWallet() {
        walletAddress = null
        chainId = null
        Log.d(TAG, "Wallet disconnected")
    }
} 