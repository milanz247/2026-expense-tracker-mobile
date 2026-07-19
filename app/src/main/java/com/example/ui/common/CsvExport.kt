package com.example.ui.common

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.network.CategoryResponse
import com.example.network.TransactionResponse
import java.io.File

/** Plain CSV of already-fetched transactions — no server-side export endpoint exists, so this is
 * built entirely on-device from data the app already loaded via `listTransactions()`. */
fun buildTransactionsCsv(transactions: List<TransactionResponse>, categoryById: Map<Long, CategoryResponse>): String {
    val header = "Date,Type,Category,Description,Amount,Fee"
    val rows = transactions.map { txn ->
        val category = categoryById[txn.categoryId]?.name ?: ""
        listOf(
            formatDisplayDate(txn.date),
            txn.type,
            category,
            txn.description,
            txn.amount.centsToMajor().toString(),
            txn.fee.centsToMajor().toString()
        ).joinToString(",") { field ->
            if (field.contains(",") || field.contains("\"")) "\"${field.replace("\"", "\"\"")}\"" else field
        }
    }
    return (listOf(header) + rows).joinToString("\n")
}

/** Writes [content] under cacheDir/exports (declared in res/xml/file_paths.xml) and hands back a
 * content:// Uri via FileProvider — a share Intent can't use a raw file:// path. */
fun writeCsvToCache(context: Context, fileName: String, content: String): Uri {
    val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(exportsDir, fileName)
    file.writeText(content)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
