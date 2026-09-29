package application.provider

import data.network.IAccountSummaryProvider
import data.network.ibkr.IbkrAccountSummaryProvider
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component

@Component
class AccountSummaryProvider(
    private val ibkrProvider: ObjectProvider<IbkrAccountSummaryProvider>
) {

    fun get(type: Type): IAccountSummaryProvider{
        return when(type){
            Type.Ibkr -> ibkrProvider.getObject()
        }
    }

    sealed interface Type {
        data object Ibkr: Type
    }
}