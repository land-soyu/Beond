//
//  BluetoothService.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import Foundation
import UIKit
import CoreBluetooth

final class BluetoothService: NSObject { // 1.
    
    static let bluetoothService = BluetoothService()
    // 2.
    let dataServiceUuid = "180A"
    let dataCharacteristicUuid = "2A29"

    let batteryServiceUuid = "180F"
    let batteryCharacteristicUuid = "2A19"
    let ledServiceUuid = "00005453-4220-7479-7065-204100000001"
    let ledCharacteristicUuid = "00005453-4220-7479-7065-204100000201"

    var ble_item:[CBPeripheral] = []
    var tableview: UITableView!
    var ble_disconnect: UIButton!
    var ble_connect_item: UILabel!
    var statusLabel: UILabel!

    
    var centralManager: CBCentralManager!
    var peripheral: CBPeripheral?
    var dataCharacteristic: CBCharacteristic?
    var batteryservice: CBService?
    var batteryCharacteristic: CBCharacteristic?
    var ledservice: CBService?
    var ledCharacteristic: CBCharacteristic?
    
    var bViewController: BLEViewController?
    var pViewController: PressureViewController?
    var fViewController: FlowViewController?
    var data_flag: Int = 0  //  1:press, 2:flow

    var bluetoothState: CBManagerState {
        return self.centralManager.state
    }
    var flowController: FlowController? // 3.
    
    override init() {
        super.init()
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    func init_setting(_ sender: UITableView, sender2: UIButton, sender3: UILabel, viewController: BLEViewController) {
        tableview = sender
        ble_disconnect = sender2
        ble_connect_item = sender3
        bViewController = viewController
    }
    func init_battery_setting(_ viewController: PressureViewController) {
        pViewController = viewController
    }
    func init_flow_setting(_ viewController: FlowViewController) {
        fViewController = viewController
    }


    func startScan() {
        self.peripheral = nil
        guard self.centralManager.state == .poweredOn else { return }
        
        self.ble_item.removeAll()
        self.tableview.reloadData()

        self.centralManager.scanForPeripherals(withServices: []) // 4.
        self.flowController?.scanStarted() // 5.
        print("scan started")
    }
    
    func stopScan() {
        self.centralManager.stopScan()
        self.flowController?.scanStopped() // 5.
        print("scan stopped\n")
    }
    
    func connect() {
        guard self.centralManager.state == .poweredOn else { return }
        guard let peripheral = self.peripheral else { return }
        self.centralManager.connect(peripheral)
    }
    
    func disconnect() {
        guard let peripheral = self.peripheral else { return }
        self.centralManager.cancelPeripheralConnection(peripheral)
        self.ledservice = nil
        self.batteryservice = nil
    }
}
