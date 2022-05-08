//
//  LEDViewController.swift
//  ble_sensor
//
//  Created by 정면진 on 05/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import UIKit

class LEDViewController: UIViewController {

    let bluetoothService: BluetoothService = BluetoothService.bluetoothService
    
    @IBOutlet weak var LedText: UILabel!
    @IBOutlet weak var LedImage: UIImageView!
    
    var led_on_off_flag: Bool = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        print(" LEDViewController viewDidLoad")
    }
    
    override func viewWillAppear(_ animated: Bool) {
        print(" LEDViewController viewWillAppear")
        if self.bluetoothService.ledservice == nil {
            LedText.isHidden = true
        } else {
            LedText.isHidden = false
        }
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

    @IBAction func Led_Click(_ sender: UIButton) {
        if self.bluetoothService.ledservice == nil { return }
        
        if led_on_off_flag {
            led_on_off_flag = false
            LedImage.image = UIImage(named: "led_off.png")
            self.bluetoothService.flowController?.readyToWrite(flag: 11) // 1.
        } else {
            led_on_off_flag = true
            LedImage.image = UIImage(named: "led_on.png")
            self.bluetoothService.flowController?.readyToWrite(flag: 12) // 1.
        }
    }
}
