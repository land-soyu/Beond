//
//  StringExtension.swift
//  ble_sensor
//
//  Created by 정면진 on 13/11/2018.
//  Copyright © 2018 정면진. All rights reserved.
//

import Foundation

extension String {
    var localized: String {
        return NSLocalizedString(self, tableName: nil, bundle: Bundle.main, value: "", comment: "")
    }
}
