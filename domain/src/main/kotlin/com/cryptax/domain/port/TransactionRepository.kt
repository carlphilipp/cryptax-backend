package com.cryptax.domain.port

import com.cryptax.domain.entity.Transaction

interface TransactionRepository {
	fun add(transaction: Transaction): Transaction
}