package com.cryptax.usecase.transaction

import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.usecase.Utils.oneTransactionWithId
import com.cryptax.usecase.Utils.oneTransactionWithId2
import io.reactivex.Maybe
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Usescase update a transaction")
@ExtendWith(MockitoExtension::class)
class UpdateTransactionTest {

    @Mock
    lateinit var transactionRepository: TransactionRepository
    @InjectMocks
    lateinit var updateTransaction: UpdateTransaction

    @Test
    fun `update transaction`() {
        // given
        val transaction = oneTransactionWithId
        given(transactionRepository.get(transaction.id)).willReturn(Maybe.just(transaction))
        given(transactionRepository.update(transaction)).willReturn(Single.just(transaction))

        // when
        val actual = updateTransaction.update(transaction).blockingGet()

        // then
        assertThat(transaction).isEqualTo(actual)
        then(transactionRepository).should().get(transaction.id)
        then(transactionRepository).should().update(transaction)
    }

    @Test
    fun `update transaction, transaction not found`() {
        // given
        val transaction = oneTransactionWithId
        given(transactionRepository.get(transaction.id)).willReturn(Maybe.empty())

        // when
        val exception = assertThrows(TransactionNotFound::class.java) {
            updateTransaction.update(transaction).blockingGet()
        }

        // then
        assertThat(transaction.id).isEqualTo(exception.message)
        then(transactionRepository).should().get(transaction.id)
        then(transactionRepository).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `update transaction, wrong user`() {
        // given
        val transaction = oneTransactionWithId
        val transactionReturned = oneTransactionWithId2
        val expected = "User [${transaction.userId}] tried to update [${transaction.id}], but that transaction is owned by [${transactionReturned.userId}]"
        given(transactionRepository.get(transaction.id)).willReturn((Maybe.just(transactionReturned)))

        // when
        val exception = assertThrows(TransactionUserDoNotMatch::class.java) {
            updateTransaction.update(transaction).blockingGet()
        }

        // then
        assertThat(expected).isEqualTo(exception.message)
        then(transactionRepository).should().get(transaction.id)
        then(transactionRepository).shouldHaveNoMoreInteractions()
    }
}
