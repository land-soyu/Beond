//
//  PressureViewController.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import UIKit
import Charts

class PressureViewController: UIViewController {

    @IBOutlet weak var pressValue: UILabel!
    @IBOutlet weak var tempValue: UILabel!
    @IBOutlet weak var pressChart: LineChartView!
    @IBOutlet weak var tempChart: LineChartView!
    @IBOutlet weak var senser_start: UIButton!
    @IBOutlet weak var press_text: UILabel!
    @IBOutlet weak var temp_text: UILabel!
    
    var pressColor:String = "2F9027"
    let bluetoothService: BluetoothService = BluetoothService.bluetoothService
    var battery_on_off_flag: Bool = false

    var pressChartEntry = [ChartDataEntry]()
    var tempChartEntry = [ChartDataEntry]()
    
    var line_press: LineChartDataSet!
    var line_temp: LineChartDataSet!
    let data_press = LineChartData()
    let data_temp = LineChartData()

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        print(" PressureViewController viewDidLoad")
        bluetoothService.init_battery_setting(self)
        senser_start.setTitle("START".localized, for: .normal)
        press_text.text = "Pressure".localized
        temp_text.text = "Temperature".localized
    }
    override func viewWillAppear(_ animated: Bool) {
        print(" PressureViewController viewWillAppear")

        self.setting_Chart()
//        self.updateGraph()
    }
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
    @IBAction func buttonClicked(_ sender: UIButton) {
        if self.bluetoothService.batteryservice == nil { return }
        
        self.bluetoothService.data_flag = 1

        if battery_on_off_flag {
            battery_on_off_flag = false
            sender.setTitle("START".localized, for: .normal)
            sender.setTitleColor(.blue, for: .normal)
            self.bluetoothService.flowController?.readyToWrite(flag: 22) // 1.
        } else {
            battery_on_off_flag = true
            sender.setTitle("STOP".localized, for: .normal)
            sender.setTitleColor(.red, for: .normal)
            self.bluetoothService.flowController?.readyToWrite(flag: 21) // 1.
        }
    }
    
    func updateGraph() {
        pressChartEntry.append(ChartDataEntry(x: Double(0), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(1), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(2), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(3), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(4), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(5), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(6), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(7), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(8), y: Double(0.0)))
        pressChartEntry.append(ChartDataEntry(x: Double(9), y: Double(0.0)))

        line_press = LineChartDataSet(values: pressChartEntry, label: "Press")
        line_press.colors = [self.hexStringToUIColor(hex: "2F9027")]
        line_press.circleColors = [self.hexStringToUIColor(hex: "2F9027")]
        line_press.circleRadius = 3
        line_press.circleHoleRadius = 2
        data_press.addDataSet(line_press)
        pressChart.data = data_press
        pressChart.reloadInputViews()

        tempChartEntry.append(ChartDataEntry(x: Double(0), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(1), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(2), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(3), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(4), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(5), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(6), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(7), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(8), y: Double(0.0)))
        tempChartEntry.append(ChartDataEntry(x: Double(9), y: Double(0.0)))
        
        line_temp = LineChartDataSet(values: tempChartEntry, label: "Temp")
        line_temp.colors = [NSUIColor.red]
        line_temp.circleColors = [NSUIColor.red]
        line_temp.circleRadius = 3
        line_temp.circleHoleRadius = 2
        data_temp.addDataSet(line_temp)
        tempChart.data = data_temp
        tempChart.reloadInputViews()
    }
    func updateGraph(_ press: Float, temp: Float) {
//        self.statusLabel.text = "inh2o \(floor(press * 100000)/100000) and temp \(floor(temp * 10)/10)"
        self.pressValue.text = "\(floor(press * 100000)/100000)"
        self.tempValue.text = "\(floor(temp * 10)/10)"
        
        if pressChartEntry.count > 10 { pressChartEntry.removeLast() }
        for i in 0..<pressChartEntry.count {
            pressChartEntry[i].x = Double(i+1)
        }
        if ( press > 2 ) {
            pressChartEntry.insert(ChartDataEntry(x: Double(0), y: 2.0), at: 0)
        } else if ( press < -2 ) {
            pressChartEntry.insert(ChartDataEntry(x: Double(0), y: -2.0), at: 0)
        } else {
            pressChartEntry.insert(ChartDataEntry(x: Double(0), y: Double(floor(press * 100000)/100000)), at: 0)
        }
        
        data_press.removeDataSet(line_press)
        line_press = LineChartDataSet(values: pressChartEntry, label: "Pressure".localized)
        line_press.colors = [self.hexStringToUIColor(hex: pressColor)]
        line_press.circleColors = [self.hexStringToUIColor(hex: pressColor)]
        line_press.circleRadius = 3
        line_press.circleHoleRadius = 2
        data_press.addDataSet(line_press)
        pressChart.data = data_press
        pressChart.reloadInputViews()
        
        if tempChartEntry.count > 10 { tempChartEntry.removeLast() }
        for i in 0..<tempChartEntry.count {
            tempChartEntry[i].x = Double(i+1)
        }
        tempChartEntry.insert(ChartDataEntry(x: Double(0), y: Double(floor(temp * 10)/10)), at: 0)

        data_temp.removeDataSet(line_temp)
        line_temp = LineChartDataSet(values: tempChartEntry, label: "Temperature".localized)
        line_temp.colors = [NSUIColor.red]
        line_temp.circleColors = [NSUIColor.red]
        line_temp.circleRadius = 3
        line_temp.circleHoleRadius = 2
        data_temp.addDataSet(line_temp)
        tempChart.data = data_temp
        tempChart.reloadInputViews()
        
    }

    func setting_Chart() {
        //  pressChart
        pressChart.xAxis.drawLabelsEnabled = false
        pressChart.xAxis.drawGridLinesEnabled = false
        
        pressChart.leftAxis.labelPosition = YAxis.LabelPosition.outsideChart
        pressChart.leftAxis.axisMaximum = 2.0
        pressChart.leftAxis.axisMinimum = -2.0
        pressChart.leftAxis.labelCount = 5
        pressChart.leftAxis.forceLabelsEnabled = true
        pressChart.leftAxis.spaceTop = 0
        
        pressChart.rightAxis.drawLabelsEnabled = false
        pressChart.rightAxis.drawAxisLineEnabled = false
        pressChart.rightAxis.drawGridLinesEnabled = false

        //  tempChart
        tempChart.xAxis.drawLabelsEnabled = false
        tempChart.xAxis.drawGridLinesEnabled = false
        
        tempChart.leftAxis.labelPosition = YAxis.LabelPosition.outsideChart
        tempChart.leftAxis.axisMaximum = 40.0
        tempChart.leftAxis.axisMinimum = 0.0
        tempChart.leftAxis.labelCount = 3
        tempChart.leftAxis.forceLabelsEnabled = false
        tempChart.leftAxis.spaceTop = 15
        
        tempChart.rightAxis.drawLabelsEnabled = false
        tempChart.rightAxis.drawAxisLineEnabled = false
        tempChart.rightAxis.drawGridLinesEnabled = false
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
