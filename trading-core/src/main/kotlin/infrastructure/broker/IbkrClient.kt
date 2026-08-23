package infrastructure.broker

import com.ib.client.*
import com.ib.client.protobuf.*
import java.lang.Exception
import application.logging.logger

class IbkrClient : EWrapper {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val signal = EJavaSignal()
    private val client = EClientSocket(
        this,
        signal
    )
    private var reader: EReader? = null
    private var readerThread: Thread? = null
    private val logger = logger<IbkrClient>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    fun connect(
        host: String = "127.0.0.1",
        port: Int = 4002,
        clientId: Int = 1
    ) {
        if(client.isConnected) {
            logger.warn("IBKR client is already connected")
            return
        }

        logger.info("Connecting to IBKR Gateway host={} port={} clientId={}", host, port, clientId)

        client.eConnect(
            host,
            port,
            clientId
        )

        if(!client.isConnected) {
            logger.error("Could not connect to IBKR Gateway host={} port={}", host, port)
            return
        }

        startMessageReader()
    }

    fun disconnect(){
        if(!client.isConnected) {
            logger.debug("IBKR client is already disconnected")
            return
        }

        logger.info("Disconnecting from IBKR Gateway")

        client.eDisconnect()

        readerThread?.interrupt()
        readerThread = null
        reader = null
    }

    fun isConnected(): Boolean{
        return client.isConnected
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    private fun startMessageReader() {
        logger.debug("Starting IBKR message reader")

        reader = EReader(
            client,
            signal
        )

        reader?.start()

        readerThread = Thread {
            while(client.isConnected){
                try{
                    signal.waitForSignal()
                    reader?.processMsgs()
                } catch(e: InterruptedException){
                    Thread.currentThread().interrupt()
                    logger.debug("IBKR reader thread interrupted")
                    break
                } catch(e: Exception){
                    logger.error("Unexpected error in IBKR reader thread", e)
                }
            }
        }.apply {
            name = "ibkr-reader"
            isDaemon = true
            start()
        }
    }


    override fun tickPrice(p0: Int, p1: Int, p2: Double, p3: TickAttrib?) {
        TODO("Not yet implemented")
    }

    override fun tickSize(p0: Int, p1: Int, p2: Decimal?) {
        TODO("Not yet implemented")
    }

    override fun tickOptionComputation(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: Double,
        p4: Double,
        p5: Double,
        p6: Double,
        p7: Double,
        p8: Double,
        p9: Double,
        p10: Double
    ) {
        TODO("Not yet implemented")
    }

    override fun tickGeneric(p0: Int, p1: Int, p2: Double) {
        TODO("Not yet implemented")
    }

    override fun tickString(p0: Int, p1: Int, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun tickEFP(
        p0: Int,
        p1: Int,
        p2: Double,
        p3: String?,
        p4: Double,
        p5: Int,
        p6: String?,
        p7: Double,
        p8: Double
    ) {
        TODO("Not yet implemented")
    }

    override fun orderStatus(
        p0: Int,
        p1: String?,
        p2: Decimal?,
        p3: Decimal?,
        p4: Double,
        p5: Long,
        p6: Int,
        p7: Double,
        p8: Int,
        p9: String?,
        p10: Double
    ) {
        TODO("Not yet implemented")
    }

    override fun openOrder(
        p0: Int,
        p1: Contract?,
        p2: Order?,
        p3: OrderState?
    ) {
        TODO("Not yet implemented")
    }

    override fun openOrderEnd() {
        TODO("Not yet implemented")
    }

    override fun updateAccountValue(p0: String?, p1: String?, p2: String?, p3: String?) {
        TODO("Not yet implemented")
    }

    override fun updatePortfolio(
        p0: Contract?,
        p1: Decimal?,
        p2: Double,
        p3: Double,
        p4: Double,
        p5: Double,
        p6: Double,
        p7: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun updateAccountTime(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun accountDownloadEnd(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun nextValidId(p0: Int) {
        logger.info("Next valid orderId={}", p0)
    }

    override fun contractDetails(p0: Int, p1: ContractDetails?) {
        TODO("Not yet implemented")
    }

    override fun bondContractDetails(p0: Int, p1: ContractDetails?) {
        TODO("Not yet implemented")
    }

    override fun contractDetailsEnd(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun execDetails(p0: Int, p1: Contract?, p2: Execution?) {
        TODO("Not yet implemented")
    }

    override fun execDetailsEnd(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun updateMktDepth(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: Int,
        p4: Double,
        p5: Decimal?
    ) {
        TODO("Not yet implemented")
    }

    override fun updateMktDepthL2(
        p0: Int,
        p1: Int,
        p2: String?,
        p3: Int,
        p4: Int,
        p5: Double,
        p6: Decimal?,
        p7: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun updateNewsBulletin(p0: Int, p1: Int, p2: String?, p3: String?) {
        TODO("Not yet implemented")
    }

    override fun managedAccounts(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun receiveFA(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun historicalData(p0: Int, p1: Bar?) {
        TODO("Not yet implemented")
    }

    override fun scannerParameters(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun scannerData(
        p0: Int,
        p1: Int,
        p2: ContractDetails?,
        p3: String?,
        p4: String?,
        p5: String?,
        p6: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun scannerDataEnd(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun realtimeBar(
        p0: Int,
        p1: Long,
        p2: Double,
        p3: Double,
        p4: Double,
        p5: Double,
        p6: Decimal?,
        p7: Decimal?,
        p8: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun currentTime(p0: Long) {
        TODO("Not yet implemented")
    }

    override fun fundamentalData(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun deltaNeutralValidation(p0: Int, p1: DeltaNeutralContract?) {
        TODO("Not yet implemented")
    }

    override fun tickSnapshotEnd(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun marketDataType(p0: Int, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun commissionAndFeesReport(p0: CommissionAndFeesReport?) {
        TODO("Not yet implemented")
    }

    override fun position(
        p0: String?,
        p1: Contract?,
        p2: Decimal?,
        p3: Double
    ) {
        TODO("Not yet implemented")
    }

    override fun positionEnd() {
        TODO("Not yet implemented")
    }

    override fun accountSummary(
        p0: Int,
        p1: String?,
        p2: String?,
        p3: String?,
        p4: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun accountSummaryEnd(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun verifyMessageAPI(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun verifyCompleted(p0: Boolean, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun verifyAndAuthMessageAPI(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun verifyAndAuthCompleted(p0: Boolean, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun displayGroupList(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun displayGroupUpdated(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun error(p0: Exception?) {
        logger.error("IBKR exception: ${p0?.message}")
    }

    override fun error(str: String?) {
        logger.error("IBKR error: $str")
    }

    override fun error(p0: Int, p1: Long, p2: Int, p3: String?, p4: String?) {
       logger.error(
            "IBKR error: id=$p0, code=$p1, message=$p2"
        )
    }

    override fun connectionClosed() {
        logger.info("IBKR connection closed")
    }

    override fun connectAck() {
        logger.info("IBKR connection acknowledged")
    }

    override fun positionMulti(
        p0: Int,
        p1: String?,
        p2: String?,
        p3: Contract?,
        p4: Decimal?,
        p5: Double
    ) {
        TODO("Not yet implemented")
    }

    override fun positionMultiEnd(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun accountUpdateMulti(
        p0: Int,
        p1: String?,
        p2: String?,
        p3: String?,
        p4: String?,
        p5: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun accountUpdateMultiEnd(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun securityDefinitionOptionalParameter(
        p0: Int,
        p1: String?,
        p2: Int,
        p3: String?,
        p4: String?,
        p5: Set<String?>?,
        p6: Set<Double?>?
    ) {
        TODO("Not yet implemented")
    }

    override fun securityDefinitionOptionalParameterEnd(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun softDollarTiers(p0: Int, p1: Array<out SoftDollarTier?>?) {
        TODO("Not yet implemented")
    }

    override fun familyCodes(p0: Array<out FamilyCode?>?) {
        TODO("Not yet implemented")
    }

    override fun symbolSamples(p0: Int, p1: Array<out ContractDescription?>?) {
        TODO("Not yet implemented")
    }

    override fun historicalDataEnd(p0: Int, p1: String?, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun mktDepthExchanges(p0: Array<out DepthMktDataDescription?>?) {
        TODO("Not yet implemented")
    }

    override fun tickNews(
        p0: Int,
        p1: Long,
        p2: String?,
        p3: String?,
        p4: String?,
        p5: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun smartComponents(
        p0: Int,
        p1: Map<Int?, Map.Entry<String?, Char?>?>?
    ) {
        TODO("Not yet implemented")
    }

    override fun tickReqParams(p0: Int, p1: Double, p2: String?, p3: Int) {
        TODO("Not yet implemented")
    }

    override fun newsProviders(p0: Array<out NewsProvider?>?) {
        TODO("Not yet implemented")
    }

    override fun newsArticle(p0: Int, p1: Int, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun historicalNews(
        p0: Int,
        p1: String?,
        p2: String?,
        p3: String?,
        p4: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun historicalNewsEnd(p0: Int, p1: Boolean) {
        TODO("Not yet implemented")
    }

    override fun headTimestamp(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun histogramData(p0: Int, p1: List<HistogramEntry?>?) {
        TODO("Not yet implemented")
    }

    override fun historicalDataUpdate(p0: Int, p1: Bar?) {
        TODO("Not yet implemented")
    }

    override fun rerouteMktDataReq(p0: Int, p1: Int, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun rerouteMktDepthReq(p0: Int, p1: Int, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun marketRule(p0: Int, p1: Array<out PriceIncrement?>?) {
        TODO("Not yet implemented")
    }

    override fun pnl(p0: Int, p1: Double, p2: Double, p3: Double) {
        TODO("Not yet implemented")
    }

    override fun pnlSingle(
        p0: Int,
        p1: Decimal?,
        p2: Double,
        p3: Double,
        p4: Double,
        p5: Double
    ) {
        TODO("Not yet implemented")
    }

    override fun historicalTicks(
        p0: Int,
        p1: List<HistoricalTick?>?,
        p2: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun historicalTicksBidAsk(
        p0: Int,
        p1: List<HistoricalTickBidAsk?>?,
        p2: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun historicalTicksLast(
        p0: Int,
        p1: List<HistoricalTickLast?>?,
        p2: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun tickByTickAllLast(
        p0: Int,
        p1: Int,
        p2: Long,
        p3: Double,
        p4: Decimal?,
        p5: TickAttribLast?,
        p6: String?,
        p7: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun tickByTickBidAsk(
        p0: Int,
        p1: Long,
        p2: Double,
        p3: Double,
        p4: Decimal?,
        p5: Decimal?,
        p6: TickAttribBidAsk?
    ) {
        TODO("Not yet implemented")
    }

    override fun tickByTickMidPoint(p0: Int, p1: Long, p2: Double) {
        TODO("Not yet implemented")
    }

    override fun orderBound(p0: Long, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun completedOrder(p0: Contract?, p1: Order?, p2: OrderState?) {
        TODO("Not yet implemented")
    }

    override fun completedOrdersEnd() {
        TODO("Not yet implemented")
    }

    override fun replaceFAEnd(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun wshMetaData(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun wshEventData(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun historicalSchedule(
        p0: Int,
        p1: String?,
        p2: String?,
        p3: String?,
        p4: List<HistoricalSession?>?
    ) {
        TODO("Not yet implemented")
    }

    override fun userInfo(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun currentTimeInMillis(p0: Long) {
        TODO("Not yet implemented")
    }

    override fun orderStatusProtoBuf(p0: OrderStatusProto.OrderStatus?) {
        TODO("Not yet implemented")
    }

    override fun openOrderProtoBuf(p0: OpenOrderProto.OpenOrder?) {
        TODO("Not yet implemented")
    }

    override fun openOrdersEndProtoBuf(p0: OpenOrdersEndProto.OpenOrdersEnd?) {
        TODO("Not yet implemented")
    }

    override fun errorProtoBuf(p0: ErrorMessageProto.ErrorMessage?) {
        TODO("Not yet implemented")
    }

    override fun execDetailsProtoBuf(p0: ExecutionDetailsProto.ExecutionDetails?) {
        TODO("Not yet implemented")
    }

    override fun execDetailsEndProtoBuf(p0: ExecutionDetailsEndProto.ExecutionDetailsEnd?) {
        TODO("Not yet implemented")
    }

    override fun completedOrderProtoBuf(p0: CompletedOrderProto.CompletedOrder?) {
        TODO("Not yet implemented")
    }

    override fun completedOrdersEndProtoBuf(p0: CompletedOrdersEndProto.CompletedOrdersEnd?) {
        TODO("Not yet implemented")
    }

    override fun orderBoundProtoBuf(p0: OrderBoundProto.OrderBound?) {
        TODO("Not yet implemented")
    }

    override fun contractDataProtoBuf(p0: ContractDataProto.ContractData?) {
        TODO("Not yet implemented")
    }

    override fun bondContractDataProtoBuf(p0: ContractDataProto.ContractData?) {
        TODO("Not yet implemented")
    }

    override fun contractDataEndProtoBuf(p0: ContractDataEndProto.ContractDataEnd?) {
        TODO("Not yet implemented")
    }

    override fun tickPriceProtoBuf(p0: TickPriceProto.TickPrice?) {
        TODO("Not yet implemented")
    }

    override fun tickSizeProtoBuf(p0: TickSizeProto.TickSize?) {
        TODO("Not yet implemented")
    }

    override fun tickOptionComputationProtoBuf(p0: TickOptionComputationProto.TickOptionComputation?) {
        TODO("Not yet implemented")
    }

    override fun tickGenericProtoBuf(p0: TickGenericProto.TickGeneric?) {
        TODO("Not yet implemented")
    }

    override fun tickStringProtoBuf(p0: TickStringProto.TickString?) {
        TODO("Not yet implemented")
    }

    override fun tickSnapshotEndProtoBuf(p0: TickSnapshotEndProto.TickSnapshotEnd?) {
        TODO("Not yet implemented")
    }

    override fun updateMarketDepthProtoBuf(p0: MarketDepthProto.MarketDepth?) {
        TODO("Not yet implemented")
    }

    override fun updateMarketDepthL2ProtoBuf(p0: MarketDepthL2Proto.MarketDepthL2?) {
        TODO("Not yet implemented")
    }

    override fun marketDataTypeProtoBuf(p0: MarketDataTypeProto.MarketDataType?) {
        TODO("Not yet implemented")
    }

    override fun tickReqParamsProtoBuf(p0: TickReqParamsProto.TickReqParams?) {
        TODO("Not yet implemented")
    }

    override fun updateAccountValueProtoBuf(p0: AccountValueProto.AccountValue?) {
        TODO("Not yet implemented")
    }

    override fun updatePortfolioProtoBuf(p0: PortfolioValueProto.PortfolioValue?) {
        TODO("Not yet implemented")
    }

    override fun updateAccountTimeProtoBuf(p0: AccountUpdateTimeProto.AccountUpdateTime?) {
        TODO("Not yet implemented")
    }

    override fun accountDataEndProtoBuf(p0: AccountDataEndProto.AccountDataEnd?) {
        TODO("Not yet implemented")
    }

    override fun managedAccountsProtoBuf(p0: ManagedAccountsProto.ManagedAccounts?) {
        TODO("Not yet implemented")
    }

    override fun positionProtoBuf(p0: PositionProto.Position?) {
        TODO("Not yet implemented")
    }

    override fun positionEndProtoBuf(p0: PositionEndProto.PositionEnd?) {
        TODO("Not yet implemented")
    }

    override fun accountSummaryProtoBuf(p0: AccountSummaryProto.AccountSummary?) {
        TODO("Not yet implemented")
    }

    override fun accountSummaryEndProtoBuf(p0: AccountSummaryEndProto.AccountSummaryEnd?) {
        TODO("Not yet implemented")
    }

    override fun positionMultiProtoBuf(p0: PositionMultiProto.PositionMulti?) {
        TODO("Not yet implemented")
    }

    override fun positionMultiEndProtoBuf(p0: PositionMultiEndProto.PositionMultiEnd?) {
        TODO("Not yet implemented")
    }

    override fun accountUpdateMultiProtoBuf(p0: AccountUpdateMultiProto.AccountUpdateMulti?) {
        TODO("Not yet implemented")
    }

    override fun accountUpdateMultiEndProtoBuf(p0: AccountUpdateMultiEndProto.AccountUpdateMultiEnd?) {
        TODO("Not yet implemented")
    }

    override fun historicalDataProtoBuf(p0: HistoricalDataProto.HistoricalData?) {
        TODO("Not yet implemented")
    }

    override fun historicalDataUpdateProtoBuf(p0: HistoricalDataUpdateProto.HistoricalDataUpdate?) {
        TODO("Not yet implemented")
    }

    override fun historicalDataEndProtoBuf(p0: HistoricalDataEndProto.HistoricalDataEnd?) {
        TODO("Not yet implemented")
    }

    override fun realTimeBarTickProtoBuf(p0: RealTimeBarTickProto.RealTimeBarTick?) {
        TODO("Not yet implemented")
    }

    override fun headTimestampProtoBuf(p0: HeadTimestampProto.HeadTimestamp?) {
        TODO("Not yet implemented")
    }

    override fun histogramDataProtoBuf(p0: HistogramDataProto.HistogramData?) {
        TODO("Not yet implemented")
    }

    override fun historicalTicksProtoBuf(p0: HistoricalTicksProto.HistoricalTicks?) {
        TODO("Not yet implemented")
    }

    override fun historicalTicksBidAskProtoBuf(p0: HistoricalTicksBidAskProto.HistoricalTicksBidAsk?) {
        TODO("Not yet implemented")
    }

    override fun historicalTicksLastProtoBuf(p0: HistoricalTicksLastProto.HistoricalTicksLast?) {
        TODO("Not yet implemented")
    }

    override fun tickByTickDataProtoBuf(p0: TickByTickDataProto.TickByTickData?) {
        TODO("Not yet implemented")
    }

    override fun updateNewsBulletinProtoBuf(p0: NewsBulletinProto.NewsBulletin?) {
        TODO("Not yet implemented")
    }

    override fun newsArticleProtoBuf(p0: NewsArticleProto.NewsArticle?) {
        TODO("Not yet implemented")
    }

    override fun newsProvidersProtoBuf(p0: NewsProvidersProto.NewsProviders?) {
        TODO("Not yet implemented")
    }

    override fun historicalNewsProtoBuf(p0: HistoricalNewsProto.HistoricalNews?) {
        TODO("Not yet implemented")
    }

    override fun historicalNewsEndProtoBuf(p0: HistoricalNewsEndProto.HistoricalNewsEnd?) {
        TODO("Not yet implemented")
    }

    override fun wshMetaDataProtoBuf(p0: WshMetaDataProto.WshMetaData?) {
        TODO("Not yet implemented")
    }

    override fun wshEventDataProtoBuf(p0: WshEventDataProto.WshEventData?) {
        TODO("Not yet implemented")
    }

    override fun tickNewsProtoBuf(p0: TickNewsProto.TickNews?) {
        TODO("Not yet implemented")
    }

    override fun scannerParametersProtoBuf(p0: ScannerParametersProto.ScannerParameters?) {
        TODO("Not yet implemented")
    }

    override fun scannerDataProtoBuf(p0: ScannerDataProto.ScannerData?) {
        TODO("Not yet implemented")
    }

    override fun fundamentalsDataProtoBuf(p0: FundamentalsDataProto.FundamentalsData?) {
        TODO("Not yet implemented")
    }

    override fun pnlProtoBuf(p0: PnLProto.PnL?) {
        TODO("Not yet implemented")
    }

    override fun pnlSingleProtoBuf(p0: PnLSingleProto.PnLSingle?) {
        TODO("Not yet implemented")
    }

    override fun receiveFAProtoBuf(p0: ReceiveFAProto.ReceiveFA?) {
        TODO("Not yet implemented")
    }

    override fun replaceFAEndProtoBuf(p0: ReplaceFAEndProto.ReplaceFAEnd?) {
        TODO("Not yet implemented")
    }

    override fun commissionAndFeesReportProtoBuf(p0: CommissionAndFeesReportProto.CommissionAndFeesReport?) {
        TODO("Not yet implemented")
    }

    override fun historicalScheduleProtoBuf(p0: HistoricalScheduleProto.HistoricalSchedule?) {
        TODO("Not yet implemented")
    }

    override fun rerouteMarketDataRequestProtoBuf(p0: RerouteMarketDataRequestProto.RerouteMarketDataRequest?) {
        TODO("Not yet implemented")
    }

    override fun rerouteMarketDepthRequestProtoBuf(p0: RerouteMarketDepthRequestProto.RerouteMarketDepthRequest?) {
        TODO("Not yet implemented")
    }

    override fun secDefOptParameterProtoBuf(p0: SecDefOptParameterProto.SecDefOptParameter?) {
        TODO("Not yet implemented")
    }

    override fun secDefOptParameterEndProtoBuf(p0: SecDefOptParameterEndProto.SecDefOptParameterEnd?) {
        TODO("Not yet implemented")
    }

    override fun softDollarTiersProtoBuf(p0: SoftDollarTiersProto.SoftDollarTiers?) {
        TODO("Not yet implemented")
    }

    override fun familyCodesProtoBuf(p0: FamilyCodesProto.FamilyCodes?) {
        TODO("Not yet implemented")
    }

    override fun symbolSamplesProtoBuf(p0: SymbolSamplesProto.SymbolSamples?) {
        TODO("Not yet implemented")
    }

    override fun smartComponentsProtoBuf(p0: SmartComponentsProto.SmartComponents?) {
        TODO("Not yet implemented")
    }

    override fun marketRuleProtoBuf(p0: MarketRuleProto.MarketRule?) {
        TODO("Not yet implemented")
    }

    override fun userInfoProtoBuf(p0: UserInfoProto.UserInfo?) {
        TODO("Not yet implemented")
    }

    override fun nextValidIdProtoBuf(p0: NextValidIdProto.NextValidId?) {
        TODO("Not yet implemented")
    }

    override fun currentTimeProtoBuf(p0: CurrentTimeProto.CurrentTime?) {
        TODO("Not yet implemented")
    }

    override fun currentTimeInMillisProtoBuf(p0: CurrentTimeInMillisProto.CurrentTimeInMillis?) {
        TODO("Not yet implemented")
    }

    override fun verifyMessageApiProtoBuf(p0: VerifyMessageApiProto.VerifyMessageApi?) {
        TODO("Not yet implemented")
    }

    override fun verifyCompletedProtoBuf(p0: VerifyCompletedProto.VerifyCompleted?) {
        TODO("Not yet implemented")
    }

    override fun displayGroupListProtoBuf(p0: DisplayGroupListProto.DisplayGroupList?) {
        TODO("Not yet implemented")
    }

    override fun displayGroupUpdatedProtoBuf(p0: DisplayGroupUpdatedProto.DisplayGroupUpdated?) {
        TODO("Not yet implemented")
    }

    override fun marketDepthExchangesProtoBuf(p0: MarketDepthExchangesProto.MarketDepthExchanges?) {
        TODO("Not yet implemented")
    }

    override fun configResponseProtoBuf(p0: ConfigResponseProto.ConfigResponse?) {
        TODO("Not yet implemented")
    }

    override fun updateConfigResponseProtoBuf(p0: UpdateConfigResponseProto.UpdateConfigResponse?) {
        TODO("Not yet implemented")
    }
}