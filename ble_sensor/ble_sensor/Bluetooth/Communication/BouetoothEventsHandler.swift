//
//  BouetoothEventsHandler.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import Foundation
import CoreBluetooth

extension BluetoothService: CBPeripheralDelegate {
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        guard let services = peripheral.services else { return }
        
        print("services discovered")
        for service in services {
            print(service)
            let serviceUuid = service.uuid.uuidString
            print("discovered service: \(serviceUuid)")
            
            if serviceUuid == self.dataServiceUuid {
                
            } else if serviceUuid == self.batteryServiceUuid {
                self.batteryservice = service
            } else if serviceUuid == self.ledServiceUuid {
                self.ledservice = service
            }

            peripheral.discoverCharacteristics(nil, for: service)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        guard let characteristics = service.characteristics else { return }
        
        print("characteristics discovered")
        for characteristic in characteristics {
            print(characteristic)
            let characteristicUuid = characteristic.uuid.uuidString
            print("discovered characteristic: \(characteristicUuid) | read=\(characteristic.properties.contains(.read)) | write=\(characteristic.properties.contains(.write))")
            
            if characteristicUuid == self.dataCharacteristicUuid {
                self.ble_disconnect.isHidden = false
                self.tableview.isHidden = true
                
                print(service.peripheral)
                if let periperalName = peripheral.name {
                    self.ble_connect_item.text = "connect is".localized + " \(periperalName)"
                } else {
                    self.ble_connect_item.text = "connect is".localized + " ble"
                }
                self.dataCharacteristic = characteristic
                self.flowController?.readyToWrite(flag: 0) // 1.
            } else if characteristicUuid == self.batteryCharacteristicUuid {
                self.batteryCharacteristic = characteristic
            } else if characteristicUuid == self.ledCharacteristicUuid {
                self.ledCharacteristic = characteristic
                self.flowController?.readyToWrite(flag: 11) // 1.
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if let data = characteristic.value {
            print("didUpdateValueFor \(characteristic.uuid.uuidString) = count: \(data.count) | \(self.hexEncodedString(data))")
            if ( data.count == 10 ) {
                let sensor_flag: String = String(Character(UnicodeScalar(data[7]))) + String(Character(UnicodeScalar(data[8]))) + String(Character(UnicodeScalar(data[9])))
                
                var raw_int: UInt16 = 0
                var raw_int_Int: Int = 0
                var raw_t: UInt16 = 0
                var raw_t_Int: Int = 0
                var inh2o: Float = 0
                var temp: Float = 0
                let FULL_SCALE_REF: UInt32 = 1 << 24
                print("sensor_flag is \(sensor_flag)")
                switch sensor_flag {
                case "DLV" :
                    raw_int = UInt16(((UInt16(data[0]) & 0x3f) << 8)) | UInt16(data[1])
                    inh2o = Float(((Double(raw_int) - 8192.0)/16384.00 )*2.00*4 )
                    raw_t = UInt16( (UInt16(data[2]) << 3) | ((UInt16(data[3]) & 0b11100000) >> 5) )
                    temp = Float(Double(raw_t) * (200.0 / 2047.0) - 50.0)
                    break
                case "DLH" :
                    raw_int_Int = Int(data[1]) << 16 | Int(data[2]) << 8 | Int(data[3])
                    inh2o = 1.25 * (((Float(raw_int_Int)-(0.5*Float(FULL_SCALE_REF)))/Float(FULL_SCALE_REF))*2.00)
                    print("raw_int_Int \(raw_int_Int) and inh2o \(inh2o) ")
                    raw_t_Int = Int(data[4]) << 16 | Int(data[5]) << 8 | Int(data[6])
                    temp = (Float(raw_t_Int) * 125.0 / Float(FULL_SCALE_REF)) - 40.0
                    print("raw_t \(raw_t_Int) and temp \(temp) ")
                    break
                case "DLC" :
                    raw_int_Int = Int(data[1]) << 16 | Int(data[2]) << 8 | Int(data[3])
                    inh2o = 1.25 * (((Float(raw_int_Int)-(0.1*Float(FULL_SCALE_REF)))/Float(FULL_SCALE_REF))*2.00)
                    print("raw_int_Int \(raw_int_Int) and inh2o \(inh2o) ")
                    raw_t_Int = Int(data[4]) << 16 | Int(data[5]) << 8 | Int(data[6])
                    temp = (Float(raw_t_Int * 150) / Float(FULL_SCALE_REF)) - 40
                    print("raw_t \(raw_t_Int) and temp \(temp) ")
                    break
                case "ADS" :
                    if ( data[0] > 15 ) {
                    } else {
                        raw_int = UInt16(data[0]) * 256 + UInt16(data[1])
                        if ( raw_int > 32767 ) {
                            raw_int -= 65535
                        }
                        inh2o = Float(raw_int)
                    }
                    break
                default :
                    break
                }
                
                print("inh2o \(floor(inh2o * 100000)/100000) and temp \(floor(temp * 10)/10)")
                if ( self.data_flag == 1 ) {
                    self.pViewController?.updateGraph(inh2o, temp: temp)
                } else {
                    self.fViewController?.updateGraph(inh2o, temp: temp)
                }
                
            } else {
                self.flowController?.received(response: data) // 1.
            }
        } else {
            print("didUpdateValueFor \(characteristic.uuid.uuidString) with no data")
        }
    }
    
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if error != nil {
            print("error while writing value to \(characteristic.uuid.uuidString): \(error.debugDescription)")
        } else {
            print("didWriteValueFor \(characteristic.uuid.uuidString)")
        }
    }
    
    private func hexEncodedString(_ data: Data?) -> String {
        let format = "%02hhX"
        return data?.map { String(format: format, $0) }.joined() ?? ""
    }

}
