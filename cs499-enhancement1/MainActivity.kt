class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var weightChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupChart()
        setupFAB()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    // Launch settings activity
                    true
                }
                R.id.action_export -> {
                    // Handle export
                    true
                }
                R.id.action_calendar -> {
                    // Show calendar view
                    true
                }
                else -> false
            }
        }
    }

    private fun setupChart() {
        weightChart = binding.weightChart
        weightChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                setDrawZeroLine(false)
            }

            axisRight.isEnabled = false
            legend.isEnabled = true

            animateX(1000)
        }

        updateChartData()
    }

    private fun updateChartData() {
        // Fetch weight entries and update chart
        val entries = ArrayList<Entry>()
        // Add weight entries here

        val dataSet = LineDataSet(entries, "Weight").apply {
            color = getColor(R.color.primary)
            setCircleColor(getColor(R.color.primary))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        weightChart.data = LineData(dataSet)
        weightChart.invalidate()
    }

    private fun setupFAB() {
        binding.fabAddWeight.setOnClickListener {
            // Show weight entry dialog or navigate to entry screen
        }
    }

    private fun setupRecyclerView() {
        binding.weightRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = WeightAdapter(/* weight entries */)
        }
    }
}