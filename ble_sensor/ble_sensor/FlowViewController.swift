//
//  FlowViewController.swift
//  ble_sensor
//
//  Created by 정면진 on 12/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import UIKit
import Charts

class FlowViewController: UIViewController {

    @IBOutlet weak var start_bt: UIButton!
    @IBOutlet weak var flowValue: UILabel!
    @IBOutlet weak var flowChart: LineChartView!
    @IBOutlet weak var flow_text: UILabel!
    
    var flowColor: String = "3186C6"
    let bluetoothService: BluetoothService = BluetoothService.bluetoothService
    var flow_on_off_flag: Bool = false

    var flowChartEntry = [ChartDataEntry]()
    
    var line_flow: LineChartDataSet!
    let data_flow = LineChartData()

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        print(" FlowViewController viewDidLoad")
        bluetoothService.init_flow_setting(self)
        start_bt.setTitle("START".localized, for: .normal)
        flow_text.text = "Flow".localized

    }
    override func viewWillAppear(_ animated: Bool) {
        print(" PressureViewController viewWillAppear")
        
        self.setting_Chart()
    }


    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

    @IBAction func buttonClicker(_ sender: UIButton) {
        if self.bluetoothService.batteryservice == nil { return }
        
        self.bluetoothService.data_flag = 2
        if flow_on_off_flag {
            flow_on_off_flag = false
            sender.setTitle("START".localized, for: .normal)
            sender.setTitleColor(.blue, for: .normal)
            self.bluetoothService.flowController?.readyToWrite(flag: 22) // 1.
        } else {
            flow_on_off_flag = true
            sender.setTitle("STOP".localized, for: .normal)
            sender.setTitleColor(.red, for: .normal)
            self.bluetoothService.flowController?.readyToWrite(flag: 21) // 1.
            
        }
    }
    
    func updateGraph(_ flow: Float, temp: Float) {
        self.flowValue.text = "\(floor(flow * 100000)/100000)"
        
        if flowChartEntry.count > 10 { flowChartEntry.removeLast() }
        for i in 0..<flowChartEntry.count {
            flowChartEntry[i].x = Double(i+1)
        }
        if ( flow > 100 ) {
            flowChartEntry.insert(ChartDataEntry(x: Double(0), y: 100.0), at: 0)
        } else if ( flow < -100 ) {
            flowChartEntry.insert(ChartDataEntry(x: Double(0), y: -100.0), at: 0)
        } else {
            flowChartEntry.insert(ChartDataEntry(x: Double(0), y: Double(floor(flow * 100000)/100000)), at: 0)
        }
        
        data_flow.removeDataSet(line_flow)
        line_flow = LineChartDataSet(values: flowChartEntry, label: "Flow".localized)
        line_flow.colors = [self.hexStringToUIColor(hex: flowColor)]
        line_flow.circleColors = [self.hexStringToUIColor(hex: flowColor)]
        line_flow.circleRadius = 3
        line_flow.circleHoleRadius = 2
        data_flow.addDataSet(line_flow)
        flowChart.data = data_flow
        flowChart.reloadInputViews()
        
    }
    
    func setting_Chart() {
        //  flowChart
        flowChart.xAxis.drawLabelsEnabled = false
        flowChart.xAxis.drawGridLinesEnabled = false
        
        flowChart.leftAxis.labelPosition = YAxis.LabelPosition.outsideChart
        flowChart.leftAxis.axisMaximum = 100.0
        flowChart.leftAxis.axisMinimum = -100.0
        flowChart.leftAxis.labelCount = 5
        flowChart.leftAxis.forceLabelsEnabled = true
        flowChart.leftAxis.spaceTop = 0
        
        flowChart.rightAxis.drawLabelsEnabled = false
        flowChart.rightAxis.drawAxisLineEnabled = false
        flowChart.rightAxis.drawGridLinesEnabled = false
    }
    
    func hexStringToUIColor (hex:String) -> NSUIColor {
        var rgbValue:UInt32 = 0
        Scanner(string: hex).scanHexInt32(&rgbValue)
        return NSUIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
}
