package gui.window

import application.logger.TextLogger
import application.runner.RunConfig
import application.runner.TaskRunner
import applyTheme
import domain.algorithm.TradingAlgorithm
import domain.interfaces.ILogger
import domain.market.security.SecurityIdentifier
import domain.tax.Taxation
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.time.Instant

class MainWindow: Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var logger: ILogger
    private lateinit var runner: TaskRunner

    private lateinit var algorithmBox: ComboBox<TradingAlgorithm.Type>
    private lateinit var taxationBox: ComboBox<Taxation.Type>

    private lateinit var isinField: TextField
    private lateinit var tickerField: TextField
    private lateinit var currencyField: TextField
    private lateinit var capitalField: TextField
    private lateinit var startDateField: TextField
    private lateinit var endDateField: TextField
    private lateinit var evaluationStepField: TextField

    private lateinit var clearTraderTestFolderBox: CheckBox

    private lateinit var logArea: TextArea
    private lateinit var actionButtons: List<Button>

    override fun start(stage: Stage) {
        stage.title = "Trading App"
        algorithmBox = ComboBox<TradingAlgorithm.Type>().apply{
            items.addAll(TradingAlgorithm.Type.entries)
            value = TradingAlgorithm.Type.TACPP46
            prefWidth = 220.0
        }

        taxationBox = ComboBox<Taxation.Type>().apply{
            items.addAll(Taxation.Type.entries)
            value = Taxation.Type.Hungary
            prefWidth = 220.0
        }

        isinField = TextField("US67066G1040")
        tickerField = TextField("NVDA")
        currencyField = TextField("USD")
        capitalField = TextField("1000.0")
        startDateField = TextField("2015-01-01")
        endDateField = TextField("2025-01-01")
        evaluationStepField = TextField("1")

        clearTraderTestFolderBox = CheckBox("Clear trader test folder")

        logArea = TextArea().apply{
            isEditable = false
            prefRowCount = 20
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        logger = TextLogger(logArea)
        runner = TaskRunner(logger)

        val form = createConfigForm()

        val backtestOneButton = Button("Backtest One Security").apply{
            setOnAction{
                runTask{
                    runner.runBacktestOnOneSecurity(readConfig())
                }
            }
        }


        actionButtons = listOf(
            backtestOneButton
        )

        val buttonRow = HBox(
            10.0,
            backtestOneButton
        ).apply{
            alignment = Pos.CENTER
        }

        val root = VBox(
            16.0,
            form,
            buttonRow,
            logArea
            ).apply{
                padding = Insets(10.0)
                alignment = Pos.CENTER_LEFT
            }

        val scene = Scene(root,1200.0, 750.0)
        applyTheme(scene, root)

        stage.scene = scene
        stage.show()
    }

    private fun createConfigForm(): GridPane{
        return GridPane().apply{
            hgap = 20.0
            vgap = 10.0
            alignment = Pos.CENTER_LEFT

            add(Label("Algorithm"),0,0)
            add(algorithmBox,1,0)

            add(Label("Taxation"),0,1)
            add(taxationBox,1,1)

            add(Label("ISIN"),0,2)
            add(isinField,1,2)

            add(Label("Ticker"),0,3)
            add(tickerField,1,3)

            add(Label("Currency"),0,4)
            add(currencyField,1,4)

            add(Label("Start Capital"), 0, 5)
            add(capitalField, 1, 5)

            add(Label("Start Date"), 0, 6)
            add(startDateField, 1, 6)

            add(Label("End Date"), 0, 7)
            add(endDateField, 1, 7)

            add(Label("Evaluation Step Years"), 0, 8)
            add(evaluationStepField, 1, 8)
        }
    }

    private fun readConfig(): RunConfig{
        val isin = isinField.text.trim()
        val ticker = tickerField.text.trim()
        val currency = currencyField.text.trim()

        require(isin.isNotBlank()) { "ISIN is empty." }
        require(ticker.isNotBlank()) { "Ticker is empty." }
        require(currency.isNotBlank()) { "Currency is empty." }

        return RunConfig(
            algorithm = requireNotNull(algorithmBox.value) {
                "No algorithm selected."
            },
            taxation = requireNotNull(taxationBox.value) {
                "No taxation selected."
            },
            identifier = SecurityIdentifier(
                isin,
                ticker,
                currency
            ),
            startCapital = capitalField.text.trim().toDouble(),
            startDate = parseDate(startDateField.text),
            endDate = parseDate(endDateField.text),
            evaluationWindowStepYears = evaluationStepField.text.trim().toInt()
        )
    }

    private fun parseDate(text: String): Instant {
        val value = text.trim()

        return if (value.contains("T")) {
            Instant.parse(value)
        } else {
            Instant.parse("${value}T00:00:00Z")
        }
    }

    private fun runTask(task: suspend() -> Unit){
        logArea.clear()
        setBusy(true)

        appScope.launch {
            try {
                task()
                logger.log("Finished")
            }catch (throwable: Throwable){
                logger.error(
                    "Task failed: ${throwable.message}",
                    throwable
                )
            }finally{
                setBusy(false)
            }
        }

    }
    private fun setBusy(value: Boolean) {
        Platform.runLater {
            actionButtons.forEach {
                it.isDisable = value
            }
        }
    }

    override fun stop() {
        appScope.cancel()
    }
}