package data.infrastructure.broker.ibkr

import com.ib.client.*
import com.ib.client.protobuf.*

interface IbkrInterfaceWrapper : EWrapper {
    //===========================================================//
    //===========================================================//
    // Default Implementation(s)

    override fun connectionClosed() { throw UnsupportedOperationException() }
    override fun connectAck() { throw UnsupportedOperationException() }
    override fun orderStatusProtoBuf(message: OrderStatusProto.OrderStatus?) { throw UnsupportedOperationException() }
    override fun openOrderProtoBuf(message: OpenOrderProto.OpenOrder?) { throw UnsupportedOperationException() }
    override fun openOrdersEndProtoBuf(message: OpenOrdersEndProto.OpenOrdersEnd?) { throw UnsupportedOperationException() }
    override fun errorProtoBuf(message: ErrorMessageProto.ErrorMessage?) { throw UnsupportedOperationException() }
    override fun execDetailsProtoBuf(message: ExecutionDetailsProto.ExecutionDetails?) { throw UnsupportedOperationException() }
    override fun execDetailsEndProtoBuf(message: ExecutionDetailsEndProto.ExecutionDetailsEnd?) { throw UnsupportedOperationException() }
    override fun completedOrderProtoBuf(p0: CompletedOrderProto.CompletedOrder?) { throw UnsupportedOperationException() }
    override fun completedOrdersEndProtoBuf(p0: CompletedOrdersEndProto.CompletedOrdersEnd?) { throw UnsupportedOperationException() }
    override fun orderBoundProtoBuf(p0: OrderBoundProto.OrderBound?) { throw UnsupportedOperationException() }
    override fun contractDataProtoBuf(p0: ContractDataProto.ContractData?) { throw UnsupportedOperationException() }
    override fun bondContractDataProtoBuf(p0: ContractDataProto.ContractData?) { throw UnsupportedOperationException() }
    override fun contractDataEndProtoBuf(p0: ContractDataEndProto.ContractDataEnd?) { throw UnsupportedOperationException() }
    override fun tickPriceProtoBuf(message: TickPriceProto.TickPrice?) { throw UnsupportedOperationException() }
    override fun tickSizeProtoBuf(p0: TickSizeProto.TickSize?) { throw UnsupportedOperationException() }
    override fun tickOptionComputationProtoBuf(p0: TickOptionComputationProto.TickOptionComputation?) { throw UnsupportedOperationException() }
    override fun tickGenericProtoBuf(p0: TickGenericProto.TickGeneric?) { throw UnsupportedOperationException() }
    override fun tickStringProtoBuf(p0: TickStringProto.TickString?) { throw UnsupportedOperationException() }
    override fun tickSnapshotEndProtoBuf(p0: TickSnapshotEndProto.TickSnapshotEnd?) { throw UnsupportedOperationException() }
    override fun updateMarketDepthProtoBuf(p0: MarketDepthProto.MarketDepth?) { throw UnsupportedOperationException() }
    override fun updateMarketDepthL2ProtoBuf(p0: MarketDepthL2Proto.MarketDepthL2?) { throw UnsupportedOperationException() }
    override fun marketDataTypeProtoBuf(p0: MarketDataTypeProto.MarketDataType?) { throw UnsupportedOperationException() }
    override fun tickReqParamsProtoBuf(p0: TickReqParamsProto.TickReqParams?) { throw UnsupportedOperationException() }
    override fun updateAccountValueProtoBuf(p0: AccountValueProto.AccountValue?) { throw UnsupportedOperationException() }
    override fun updatePortfolioProtoBuf(p0: PortfolioValueProto.PortfolioValue?) { throw UnsupportedOperationException() }
    override fun updateAccountTimeProtoBuf(p0: AccountUpdateTimeProto.AccountUpdateTime?) { throw UnsupportedOperationException() }
    override fun accountDataEndProtoBuf(p0: AccountDataEndProto.AccountDataEnd?) { throw UnsupportedOperationException() }
    override fun managedAccountsProtoBuf(message: ManagedAccountsProto.ManagedAccounts?) { throw UnsupportedOperationException() }
    override fun positionProtoBuf(p0: PositionProto.Position?) { throw UnsupportedOperationException() }
    override fun positionEndProtoBuf(p0: PositionEndProto.PositionEnd?) { throw UnsupportedOperationException() }
    override fun accountSummaryProtoBuf(message: AccountSummaryProto.AccountSummary?) { throw UnsupportedOperationException() }
    override fun accountSummaryEndProtoBuf(p0: AccountSummaryEndProto.AccountSummaryEnd?) { throw UnsupportedOperationException() }
    override fun positionMultiProtoBuf(p0: PositionMultiProto.PositionMulti?) { throw UnsupportedOperationException() }
    override fun positionMultiEndProtoBuf(p0: PositionMultiEndProto.PositionMultiEnd?) { throw UnsupportedOperationException() }
    override fun accountUpdateMultiProtoBuf(p0: AccountUpdateMultiProto.AccountUpdateMulti?) { throw UnsupportedOperationException() }
    override fun accountUpdateMultiEndProtoBuf(p0: AccountUpdateMultiEndProto.AccountUpdateMultiEnd?) { throw UnsupportedOperationException() }
    override fun historicalDataProtoBuf(message: HistoricalDataProto.HistoricalData?) { throw UnsupportedOperationException() }
    override fun historicalDataUpdateProtoBuf(p0: HistoricalDataUpdateProto.HistoricalDataUpdate?) { throw UnsupportedOperationException() }
    override fun historicalDataEndProtoBuf(message: HistoricalDataEndProto.HistoricalDataEnd?) { throw UnsupportedOperationException() }
    override fun realTimeBarTickProtoBuf(p0: RealTimeBarTickProto.RealTimeBarTick?) { throw UnsupportedOperationException() }
    override fun headTimestampProtoBuf(p0: HeadTimestampProto.HeadTimestamp?) { throw UnsupportedOperationException() }
    override fun histogramDataProtoBuf(p0: HistogramDataProto.HistogramData?) { throw UnsupportedOperationException() }
    override fun historicalTicksProtoBuf(p0: HistoricalTicksProto.HistoricalTicks?) { throw UnsupportedOperationException() }
    override fun historicalTicksBidAskProtoBuf(p0: HistoricalTicksBidAskProto.HistoricalTicksBidAsk?) { throw UnsupportedOperationException() }
    override fun historicalTicksLastProtoBuf(p0: HistoricalTicksLastProto.HistoricalTicksLast?) { throw UnsupportedOperationException() }
    override fun tickByTickDataProtoBuf(p0: TickByTickDataProto.TickByTickData?) { throw UnsupportedOperationException() }
    override fun updateNewsBulletinProtoBuf(p0: NewsBulletinProto.NewsBulletin?) { throw UnsupportedOperationException() }
    override fun newsArticleProtoBuf(p0: NewsArticleProto.NewsArticle?) { throw UnsupportedOperationException() }
    override fun newsProvidersProtoBuf(p0: NewsProvidersProto.NewsProviders?) { throw UnsupportedOperationException() }
    override fun historicalNewsProtoBuf(p0: HistoricalNewsProto.HistoricalNews?) { throw UnsupportedOperationException() }
    override fun historicalNewsEndProtoBuf(p0: HistoricalNewsEndProto.HistoricalNewsEnd?) { throw UnsupportedOperationException() }
    override fun wshMetaDataProtoBuf(p0: WshMetaDataProto.WshMetaData?) { throw UnsupportedOperationException() }
    override fun wshEventDataProtoBuf(p0: WshEventDataProto.WshEventData?) { throw UnsupportedOperationException() }
    override fun tickNewsProtoBuf(p0: TickNewsProto.TickNews?) { throw UnsupportedOperationException() }
    override fun scannerParametersProtoBuf(p0: ScannerParametersProto.ScannerParameters?) { throw UnsupportedOperationException() }
    override fun scannerDataProtoBuf(p0: ScannerDataProto.ScannerData?) { throw UnsupportedOperationException() }
    override fun fundamentalsDataProtoBuf(p0: FundamentalsDataProto.FundamentalsData?) { throw UnsupportedOperationException() }
    override fun pnlProtoBuf(p0: PnLProto.PnL?) { throw UnsupportedOperationException() }
    override fun pnlSingleProtoBuf(p0: PnLSingleProto.PnLSingle?) { throw UnsupportedOperationException() }
    override fun receiveFAProtoBuf(p0: ReceiveFAProto.ReceiveFA?) { throw UnsupportedOperationException() }
    override fun replaceFAEndProtoBuf(p0: ReplaceFAEndProto.ReplaceFAEnd?) { throw UnsupportedOperationException() }
    override fun commissionAndFeesReportProtoBuf(p0: CommissionAndFeesReportProto.CommissionAndFeesReport?) { throw UnsupportedOperationException() }
    override fun historicalScheduleProtoBuf(p0: HistoricalScheduleProto.HistoricalSchedule?) { throw UnsupportedOperationException() }
    override fun rerouteMarketDataRequestProtoBuf(p0: RerouteMarketDataRequestProto.RerouteMarketDataRequest?) { throw UnsupportedOperationException() }
    override fun rerouteMarketDepthRequestProtoBuf(p0: RerouteMarketDepthRequestProto.RerouteMarketDepthRequest?) { throw UnsupportedOperationException() }
    override fun secDefOptParameterProtoBuf(p0: SecDefOptParameterProto.SecDefOptParameter?) { throw UnsupportedOperationException() }
    override fun secDefOptParameterEndProtoBuf(p0: SecDefOptParameterEndProto.SecDefOptParameterEnd?) { throw UnsupportedOperationException() }
    override fun softDollarTiersProtoBuf(p0: SoftDollarTiersProto.SoftDollarTiers?) { throw UnsupportedOperationException() }
    override fun familyCodesProtoBuf(p0: FamilyCodesProto.FamilyCodes?) { throw UnsupportedOperationException() }
    override fun symbolSamplesProtoBuf(p0: SymbolSamplesProto.SymbolSamples?) { throw UnsupportedOperationException() }
    override fun smartComponentsProtoBuf(p0: SmartComponentsProto.SmartComponents?) { throw UnsupportedOperationException() }
    override fun marketRuleProtoBuf(p0: MarketRuleProto.MarketRule?) { throw UnsupportedOperationException() }
    override fun userInfoProtoBuf(p0: UserInfoProto.UserInfo?) { throw UnsupportedOperationException() }
    override fun nextValidIdProtoBuf(message: NextValidIdProto.NextValidId?) { throw UnsupportedOperationException() }
    override fun currentTimeProtoBuf(p0: CurrentTimeProto.CurrentTime?) { throw UnsupportedOperationException() }
    override fun currentTimeInMillisProtoBuf(p0: CurrentTimeInMillisProto.CurrentTimeInMillis?) { throw UnsupportedOperationException() }
    override fun verifyMessageApiProtoBuf(p0: VerifyMessageApiProto.VerifyMessageApi?) { throw UnsupportedOperationException() }
    override fun verifyCompletedProtoBuf(p0: VerifyCompletedProto.VerifyCompleted?) { throw UnsupportedOperationException() }
    override fun displayGroupListProtoBuf(p0: DisplayGroupListProto.DisplayGroupList?) { throw UnsupportedOperationException() }
    override fun displayGroupUpdatedProtoBuf(p0: DisplayGroupUpdatedProto.DisplayGroupUpdated?) { throw UnsupportedOperationException() }
    override fun marketDepthExchangesProtoBuf(p0: MarketDepthExchangesProto.MarketDepthExchanges?) { throw UnsupportedOperationException() }
    override fun configResponseProtoBuf(p0: ConfigResponseProto.ConfigResponse?) { throw UnsupportedOperationException() }
    override fun updateConfigResponseProtoBuf(p0: UpdateConfigResponseProto.UpdateConfigResponse?) { throw UnsupportedOperationException() }

    //===========================================================//
    //===========================================================//
    // LEGACY

    override fun tickPrice(tickerId: Int, field: Int, price: Double, attrib: TickAttrib?) { throw UnsupportedOperationException() }
    override fun tickSize(p0: Int, p1: Int, p2: Decimal?) { throw UnsupportedOperationException() }
    override fun tickOptionComputation(p0: Int, p1: Int, p2: Int, p3: Double, p4: Double, p5: Double, p6: Double, p7: Double, p8: Double, p9: Double, p10: Double) { throw UnsupportedOperationException() }
    override fun tickGeneric(p0: Int, p1: Int, p2: Double) { throw UnsupportedOperationException() }
    override fun tickString(p0: Int, p1: Int, p2: String?) { throw UnsupportedOperationException() }
    override fun tickEFP(p0: Int, p1: Int, p2: Double, p3: String?, p4: Double, p5: Int, p6: String?, p7: Double, p8: Double) { throw UnsupportedOperationException() }
    override fun orderStatus(p0: Int, p1: String?, p2: Decimal?, p3: Decimal?, p4: Double, p5: Long, p6: Int, p7: Double, p8: Int, p9: String?, p10: Double) { throw UnsupportedOperationException() }
    override fun openOrder(orderId: Int, contract: Contract?, order: Order?, orderState: OrderState?) { throw UnsupportedOperationException() }
    override fun openOrderEnd() { throw UnsupportedOperationException() }
    override fun updateAccountValue(p0: String?, p1: String?, p2: String?, p3: String?) { throw UnsupportedOperationException() }
    override fun updatePortfolio(p0: Contract?, p1: Decimal?, p2: Double, p3: Double, p4: Double, p5: Double, p6: Double, p7: String?) { throw UnsupportedOperationException() }
    override fun updateAccountTime(p0: String?) { throw UnsupportedOperationException() }
    override fun accountDownloadEnd(p0: String?) { throw UnsupportedOperationException() }
    override fun nextValidId(orderId: Int) { throw UnsupportedOperationException() }
    override fun contractDetails(p0: Int, p1: ContractDetails?) { throw UnsupportedOperationException() }
    override fun bondContractDetails(p0: Int, p1: ContractDetails?) { throw UnsupportedOperationException() }
    override fun contractDetailsEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun execDetails(reqId: Int, contract: Contract?, execution: Execution?) { throw UnsupportedOperationException() }
    override fun execDetailsEnd(reqId: Int) { throw UnsupportedOperationException() }
    override fun updateMktDepth(p0: Int, p1: Int, p2: Int, p3: Int, p4: Double, p5: Decimal?) { throw UnsupportedOperationException() }
    override fun updateMktDepthL2(p0: Int, p1: Int, p2: String?, p3: Int, p4: Int, p5: Double, p6: Decimal?, p7: Boolean) { throw UnsupportedOperationException() }
    override fun updateNewsBulletin(p0: Int, p1: Int, p2: String?, p3: String?) { throw UnsupportedOperationException() }
    override fun managedAccounts(p0: String?) { throw UnsupportedOperationException() }
    override fun receiveFA(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun historicalData(p0: Int, p1: Bar?) { throw UnsupportedOperationException() }
    override fun scannerParameters(p0: String?) { throw UnsupportedOperationException() }
    override fun scannerData(p0: Int, p1: Int, p2: ContractDetails?, p3: String?, p4: String?, p5: String?, p6: String?) { throw UnsupportedOperationException() }
    override fun scannerDataEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun realtimeBar(p0: Int, p1: Long, p2: Double, p3: Double, p4: Double, p5: Double, p6: Decimal?, p7: Decimal?, p8: Int) { throw UnsupportedOperationException() }
    override fun currentTime(p0: Long) { throw UnsupportedOperationException() }
    override fun fundamentalData(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun deltaNeutralValidation(p0: Int, p1: DeltaNeutralContract?) { throw UnsupportedOperationException() }
    override fun tickSnapshotEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun marketDataType(p0: Int, p1: Int) { throw UnsupportedOperationException() }
    override fun commissionAndFeesReport(p0: CommissionAndFeesReport?) { throw UnsupportedOperationException() }
    override fun position(p0: String?, p1: Contract?, p2: Decimal?, p3: Double) { throw UnsupportedOperationException() }
    override fun positionEnd() { throw UnsupportedOperationException() }
    override fun accountSummary(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?) { throw UnsupportedOperationException() }
    override fun accountSummaryEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun verifyMessageAPI(p0: String?) { throw UnsupportedOperationException() }
    override fun verifyCompleted(p0: Boolean, p1: String?) { throw UnsupportedOperationException() }
    override fun verifyAndAuthMessageAPI(p0: String?, p1: String?) { throw UnsupportedOperationException() }
    override fun verifyAndAuthCompleted(p0: Boolean, p1: String?) { throw UnsupportedOperationException() }
    override fun displayGroupList(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun displayGroupUpdated(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun error(exception: Exception?) { throw UnsupportedOperationException() }
    override fun error(message: String?) { throw UnsupportedOperationException() }
    override fun error(p0: Int, p1: Long, p2: Int, p3: String?, p4: String?) { throw UnsupportedOperationException() }
    override fun positionMulti(p0: Int, p1: String?, p2: String?, p3: Contract?, p4: Decimal?, p5: Double) { throw UnsupportedOperationException() }
    override fun positionMultiEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun accountUpdateMulti(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?, p5: String?) { throw UnsupportedOperationException() }
    override fun accountUpdateMultiEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun securityDefinitionOptionalParameter(p0: Int, p1: String?, p2: Int, p3: String?, p4: String?, p5: Set<String?>?, p6: Set<Double?>?) { throw UnsupportedOperationException() }
    override fun securityDefinitionOptionalParameterEnd(p0: Int) { throw UnsupportedOperationException() }
    override fun softDollarTiers(p0: Int, p1: Array<out SoftDollarTier?>?) { throw UnsupportedOperationException() }
    override fun familyCodes(p0: Array<out FamilyCode?>?) { throw UnsupportedOperationException() }
    override fun symbolSamples(p0: Int, p1: Array<out ContractDescription?>?) { throw UnsupportedOperationException() }
    override fun historicalDataEnd(p0: Int, p1: String?, p2: String?) { throw UnsupportedOperationException() }
    override fun mktDepthExchanges(p0: Array<out DepthMktDataDescription?>?) { throw UnsupportedOperationException() }
    override fun tickNews(p0: Int, p1: Long, p2: String?, p3: String?, p4: String?, p5: String?) { throw UnsupportedOperationException() }
    override fun smartComponents(p0: Int, p1: Map<Int?, Map.Entry<String?, Char?>?>?) { throw UnsupportedOperationException() }
    override fun tickReqParams(p0: Int, p1: Double, p2: String?, p3: Int) { throw UnsupportedOperationException() }
    override fun newsProviders(p0: Array<out NewsProvider?>?) { throw UnsupportedOperationException() }
    override fun newsArticle(p0: Int, p1: Int, p2: String?) { throw UnsupportedOperationException() }
    override fun historicalNews(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?) { throw UnsupportedOperationException() }
    override fun historicalNewsEnd(p0: Int, p1: Boolean) { throw UnsupportedOperationException() }
    override fun headTimestamp(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun histogramData(p0: Int, p1: List<HistogramEntry?>?) { throw UnsupportedOperationException() }
    override fun historicalDataUpdate(p0: Int, p1: Bar?) { throw UnsupportedOperationException() }
    override fun rerouteMktDataReq(p0: Int, p1: Int, p2: String?) { throw UnsupportedOperationException() }
    override fun rerouteMktDepthReq(p0: Int, p1: Int, p2: String?) { throw UnsupportedOperationException() }
    override fun marketRule(p0: Int, p1: Array<out PriceIncrement?>?) { throw UnsupportedOperationException() }
    override fun pnl(p0: Int, p1: Double, p2: Double, p3: Double) { throw UnsupportedOperationException() }
    override fun pnlSingle(p0: Int, p1: Decimal?, p2: Double, p3: Double, p4: Double, p5: Double) { throw UnsupportedOperationException() }
    override fun historicalTicks(p0: Int, p1: List<HistoricalTick?>?, p2: Boolean) { throw UnsupportedOperationException() }
    override fun historicalTicksBidAsk(p0: Int, p1: List<HistoricalTickBidAsk?>?, p2: Boolean) { throw UnsupportedOperationException() }
    override fun historicalTicksLast(p0: Int, p1: List<HistoricalTickLast?>?, p2: Boolean) { throw UnsupportedOperationException() }
    override fun tickByTickAllLast(p0: Int, p1: Int, p2: Long, p3: Double, p4: Decimal?, p5: TickAttribLast?, p6: String?, p7: String?) { throw UnsupportedOperationException() }
    override fun tickByTickBidAsk(p0: Int, p1: Long, p2: Double, p3: Double, p4: Decimal?, p5: Decimal?, p6: TickAttribBidAsk?) { throw UnsupportedOperationException() }
    override fun tickByTickMidPoint(p0: Int, p1: Long, p2: Double) { throw UnsupportedOperationException() }
    override fun orderBound(p0: Long, p1: Int, p2: Int) { throw UnsupportedOperationException() }
    override fun completedOrder(p0: Contract?, p1: Order?, p2: OrderState?) { throw UnsupportedOperationException() }
    override fun completedOrdersEnd() { throw UnsupportedOperationException() }
    override fun replaceFAEnd(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun wshMetaData(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun wshEventData(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun historicalSchedule(p0: Int, p1: String?, p2: String?, p3: String?, p4: List<HistoricalSession?>?) { throw UnsupportedOperationException() }
    override fun userInfo(p0: Int, p1: String?) { throw UnsupportedOperationException() }
    override fun currentTimeInMillis(p0: Long) { throw UnsupportedOperationException() }
}