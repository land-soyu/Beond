//
//  BluetoothCommads.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import Foundation
import CoreBluetooth

extension BluetoothService {
    
    func getSettings() {
        self.peripheral?.readValue(for: self.dataCharacteristic!)
    }
    func getLedOnSettings() {
        
        var txBuff: Int = 0x01
        let ns = NSData(bytes: &txBuff, length: MemoryLayout<Int8>.size)
        self.peripheral?.writeValue(ns as Data, for: self.ledCharacteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    func getLedOffSettings() {
        
        var txBuff: Int = 0x00
        let ns = NSData(bytes: &txBuff, length: MemoryLayout<Int8>.size)
        self.peripheral?.writeValue(ns as Data, for: self.ledCharacteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    func getBatteryOnSettings() {
        self.peripheral?.setNotifyValue(true, for: self.batteryCharacteristic!)
    }
    func getBatteryOffSettings() {
        self.peripheral?.setNotifyValue(false, for: self.batteryCharacteristic!)
    }

    // TODO: add other methods to expose high level requests to peripheral
}
