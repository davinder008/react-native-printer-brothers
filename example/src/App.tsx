import React from 'react';
import { StyleSheet, View, Text, FlatList } from 'react-native';
import {
  getConnectedBluetoothDevices,
  connectPrinter,
  printImageWithBlueToothPrinter,
} from 'react-native-printer-brothers';

export default function App() {
  const [result, setResult] = React.useState([]);

  React.useEffect(() => {
    // getConnectedBluetoothDevices()
    //   .then((response) => {
    //     setResult(JSON.parse(response));
    //     console.log('getConnectedBluetoothDevices', JSON.parse(response));
    //   })
    //   .catch((error) => console.log('error', error));

    // connectPrinter('3C:07:84:D8:D1:EE')
    //   .then((response) => {
    //     console.log('connectPrinter', response);
    //   })
    //   .catch((error) => console.log('error', error));

    printImageWithBlueToothPrinter('3C:07:84:D8:D1:EE', '')
      .then((response) => {
        console.log('printImageWithBlueToothPrinter', response);
      })
      .catch((error) => console.log('error >>>>>>', error));
  }, []);

  const renderItems = ({ item, index }) => {
    return (
      <View>
        <Text>
          {item.name + ' - '}
          <Text>{item.macAddress}</Text>
        </Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={result}
        extraData={result}
        renderItem={renderItems}
        keyExtractor={(item, index) => index.toString()}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
