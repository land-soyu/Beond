//
//  FlowController.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import Foundation
import CoreBluetooth

class FlowController {
    
    weak var bluetoothSerivce: BluetoothService? // 1.
    
    init(bluetoothSerivce: BluetoothService) {
        self.bluetoothSerivce = bluetoothSerivce
    }
    
    func bluetoothOn() {
    }
    
    func bluetoothOff() {
    }
    
    func scanStarted() {
    }
    
    func scanStopped() {
    }
    
    func connected(peripheral: CBPeripheral) {
    }
    
    func disconnected(failure: Bool) {
    }
    
    func discoveredPeripheral() {
    }
    
    func readyToWrite(flag: Int) {
    }
    
    func received(response: Data) {
    }
    
    // TODO: add other events if needed
}
