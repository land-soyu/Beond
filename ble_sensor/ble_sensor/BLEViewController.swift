//
//  BLEViewController.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import UIKit
import CoreBluetooth

class BLEViewController: UIViewController, UITableViewDataSource, UITableViewDelegate  {

    @IBOutlet weak var loadingimg: UIActivityIndicatorView!
    @IBOutlet weak var tableview: UITableView!
    @IBOutlet weak var ble_scan: UIButton!
    @IBOutlet weak var ble_disconnect: UIButton!
    @IBOutlet weak var ble_connect_item: UILabel!
    
    var scanflag = false

    let bluetoothService: BluetoothService = BluetoothService.bluetoothService
    lazy var pairingFlow = PairingFlow(bluetoothSerivce: self.bluetoothService)

    public func returnBluetoothService()-> BluetoothService {
        return bluetoothService
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.

        print(" BLEViewController viewDidLoad")
        bluetoothService.flowController = self.pairingFlow

        ble_scan.setTitle("BLE SCAN".localized, for: .normal)
        ble_disconnect.setTitle("DISCONNECT".localized, for: .normal)
        ble_disconnect.setTitleColor(.red, for: .normal)
        ble_connect_item.text = "No ble connection.".localized
        ble_disconnect.isHidden = true
        tableview.isHidden = true

        tableview.delegate = self
        tableview.dataSource = self

        bluetoothService.init_setting(tableview, sender2: ble_disconnect, sender3: ble_connect_item, viewController: self)
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

    
    
    @IBAction func ble_scan_init(_ sender: UIButton) {
        if self.bluetoothService.centralManager.state == .poweredOff {
            let alertController = UIAlertController(title: "Bluetooth", message: "Bluetooth is turned off. Must be turned on for normal use. Do you want to go to the settings screen?".localized, preferredStyle: .alert)
            let okAction = UIAlertAction(title: "OK", style: .destructive){ (action: UIAlertAction) in
                if let url = URL(string: "App-prefs:root=Bluetooth") {
                    if #available(iOS 10.0, *) {
                        UIApplication.shared.open(url, options: [:], completionHandler: nil)
                    } else {
                        UIApplication.shared.openURL(url)
                    }
                }
            }
            alertController.addAction(okAction)
            let cancelAction = UIAlertAction(title: "Cancel".localized, style: .cancel, handler: nil)
            alertController.addAction(cancelAction)
            
            self.present(alertController, animated: true, completion: nil)

        } else {
            print(" ble_scan_init")
            tableview.isHidden = false
            if ( !scanflag ) {
                self.ble_connect_item.text = "Start scanning ble".localized
                print(" ble_scan_start")
                bluetoothService.ble_item.removeAll()
                
                self.pairingFlow.waitForPeripheral {
                }
                
                sender.setTitle("STOP SCAN".localized, for: .normal)
                sender.setTitleColor(.red, for: .normal)
                scanflag = true
                loadingimg.startAnimating()
            } else {
                self.ble_connect_item.text = "End scanning ble".localized
                print(" ble_scan_end")
                
                self.pairingFlow.cancel()
                sender.setTitle("BLE SCAN".localized, for: .normal)
                sender.setTitleColor(.blue, for: .normal)
                scanflag = false
                loadingimg.stopAnimating()
            }
        }
    }
    func ble_scan_end() {
        self.ble_connect_item.text = "End scanning ble".localized
        print(" ble_scan_end")

        self.pairingFlow.cancel()
        ble_scan.setTitle("BLE SCAN".localized, for: .normal)
        ble_scan.setTitleColor(.blue, for: .normal)
        scanflag = false
        loadingimg.stopAnimating()
    }
    func ble_connect(_ row: Int) {
        ble_scan_end()
        bluetoothService.peripheral = bluetoothService.ble_item[row]
        
        self.pairingFlow.pair { result in // continue with next step
            self.ble_connect_item.text = "Status: pairing \(result ? "successful" : "failed")"
            if !result {
                self.ble_scan_end()
            }
        }

    }
    @IBAction func ble_disconnect(_ sender: UIButton) {
        print(" ble_disconnect")
        sender.isHidden = true
        ble_connect_item.text = "No ble connection.".localized
        self.pairingFlow.cancel()
    }
    func ble_disconnecting() {
        print(" ble_disconnect")
        ble_disconnect.isHidden = true
        ble_connect_item.text = "No ble connection.".localized
        self.pairingFlow.cancel()
    }
    
    
    
    
    
    
    
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return bluetoothService.ble_item.count
    }
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell()
        cell.textLabel?.text = bluetoothService.ble_item[indexPath.row].name
        return cell
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if ( bluetoothService.ble_item.count > indexPath.row ) {
            let alertController = UIAlertController(title: "BLE CONNNECT".localized, message: bluetoothService.ble_item[indexPath.row].name, preferredStyle: .alert)
            let okAction = UIAlertAction(title: "Connect".localized, style: .destructive){ (action: UIAlertAction) in
                print("ble connect")
                self.ble_connect(indexPath.row)
            }
            alertController.addAction(okAction)
            let cancelAction = UIAlertAction(title: "Cancel".localized, style: .cancel, handler: nil)
            alertController.addAction(cancelAction)
            
            self.present(alertController, animated: true, completion: nil)
        }
    }

}
 
