//
//  PairingFlow.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import Foundation
import CoreBluetooth

class PairingFlow: FlowController {
    
    let timeout = 15.0
    var waitForPeripheralHandler: () -> Void = { }
    var pairingHandler: (Bool) -> Void = { _ in }
    var pairingWorkitem: DispatchWorkItem?
    var pairing = false
    
    // MARK: 1. Pairing steps
    
    func waitForPeripheral(completion: @escaping () -> Void) {
        self.pairing = false
        self.pairingHandler = { _ in }
        
        self.bluetoothSerivce?.startScan()
        self.waitForPeripheralHandler = completion
    }
    
    func pair(completion: @escaping (Bool) -> Void) {
        guard self.bluetoothSerivce?.centralManager.state == .poweredOn else {
            print("bluetooth is off")
            self.pairingFailed()
            return
        }
        guard let peripheral = self.bluetoothSerivce?.peripheral else {
            print("peripheral not found")
            self.pairingFailed()
            return
        }
        
        self.pairing = true
        self.pairingWorkitem = DispatchWorkItem { // 2.
            print("pairing timed out")
            self.pairingFailed()
        }
//        DispatchQueue.main.asyncAfter(deadline: .now() + self.timeout, execute: self.pairingWorkitem!) // 2.
        
        print("pairing...")
        self.pairingHandler = completion
        self.bluetoothSerivce?.centralManager.connect(peripheral, options: nil)
    }
    
    func cancel() {
        self.bluetoothSerivce?.stopScan()
        self.bluetoothSerivce?.disconnect()
        self.pairingWorkitem?.cancel()
        
        self.pairing = false
        self.pairingHandler = { _ in }
        self.waitForPeripheralHandler = { }
    }
    
    // MARK: 3. State handling
    
    override func discoveredPeripheral() {
        self.bluetoothSerivce?.stopScan()
        self.waitForPeripheralHandler()
    }
    
    override func readyToWrite(flag: Int) {
        guard self.pairing else { return }
        
        switch flag {
        case 11:
            self.bluetoothSerivce?.getLedOffSettings() // 4.
            break
        case 12:
            self.bluetoothSerivce?.getLedOnSettings() // 4.
            break
        case 21:
            self.bluetoothSerivce?.getBatteryOnSettings() // 4.
            break
        case 22:
            self.bluetoothSerivce?.getBatteryOffSettings() // 4.
            break
        default:
            self.bluetoothSerivce?.getSettings() // 4.
            break
        }
    }
    
    override func received(response: Data) {
        var responseData = String(bytes: response, encoding: String.Encoding.ascii)
        print("received data: \(responseData ?? "")") // 5.
        // TODO: validate response to confirm that pairing is sucessful
//        self.pairingHandler(true)
//        self.cancel()
        
        
    }
    
    override func disconnected(failure: Bool) {
        self.pairingFailed()
    }
    
    private func pairingFailed() {
        self.pairingHandler(false)
        self.cancel()
    }
}
