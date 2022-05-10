import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-printer-brothers' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const BROTHER_PRINTER = NativeModules.BROTHER_PRINTER
  ? NativeModules.BROTHER_PRINTER
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function getConnectedBluetoothDevices(): Promise<string> {
  return BROTHER_PRINTER.getConnectedBluetoothDevices();
}

export function connectPrinter(macAddress: string): Promise<string> {
  return BROTHER_PRINTER.connectPrinter(macAddress);
}

export function printImageWithBlueToothPrinter(
  macAddress: string,
  filePath: string
): Promise<string> {
  return BROTHER_PRINTER.printImageWithBlueToothPrinter(macAddress, filePath);
}
