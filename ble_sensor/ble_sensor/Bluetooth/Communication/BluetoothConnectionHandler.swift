//
//  BluetoothConnectionHandler.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import Foundation
import CoreBluetooth

extension BluetoothService: CBCentralManagerDelegate {
    
    var expectedNamePrefix: String { return "" } // 1.
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state != .poweredOn {
            print("bluetooth is OFF (\(central.state.rawValue))")
            self.stopScan()
            self.disconnect()
            self.flowController?.bluetoothOff() // 2.
        } else {
            print("bluetooth is ON")
            self.flowController?.bluetoothOn() // 2.
        }
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        print("centralManager discovered peripheral")
        print(peripheral)
        guard peripheral.name != nil && peripheral.name?.starts(with: self.expectedNamePrefix) ?? false else { return } // 1.
        print("discovered peripheral: \(peripheral.name!)")
        print("discovered peripheral: \(peripheral.state)")
        print("discovered peripheral: \(peripheral.debugDescription)")
        print("discovered peripheral: \(peripheral.description)")
        print("discovered peripheral: \(peripheral.identifier)")

        self.peripheral = peripheral
        self.ble_item.append(peripheral)
        self.tableview.reloadData()
//        self.flowController?.discoveredPeripheral()
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        if let periperalName = peripheral.name {
            print("connected to: \(periperalName)")
        } else {
            print("connected to peripheral")
        }
        
        peripheral.delegate = self
        peripheral.discoverServices(nil)
        self.flowController?.connected(peripheral: peripheral) // 2.
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("peripheral disconnected")
        self.dataCharacteristic = nil
        self.flowController?.disconnected(failure: false) // 2.
        
        self.bViewController?.ble_disconnecting()
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("failed to connect: \(error.debugDescription)")
        self.dataCharacteristic = nil
        self.flowController?.disconnected(failure: true) // 2.
    }
}
