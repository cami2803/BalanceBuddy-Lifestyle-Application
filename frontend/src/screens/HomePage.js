import React from 'react';
import { View, Text, TouchableOpacity, Alert } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import styles from '../styles/HomeStyle';
import API_BASE_URL from '../utils/environment_variables';

const HomePage = ({ navigation }) => {
    const handleLogout = async () => {
        try {
            const accessToken = await AsyncStorage.getItem('accessToken');
            const response = await fetch(`${API_BASE_URL}/auth/logout`, { 
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${accessToken}`
                },
            });
            if (response.ok) {
                const contentType = response.headers.get('Content-Type');
                if (contentType && contentType.includes('application/json')) {
                    const data = await response.json(); 
                    Alert.alert("Success", data.message || "You have been logged out.");
                } else {
                    Alert.alert("Success", "You have been logged out.");
                }
                await AsyncStorage.removeItem('accessToken');
                navigation.navigate('LoginPage');
            } else {
                const errorData = await response.json().catch(() => ({}));
                Alert.alert("Error", errorData.error || "Logout failed");
            }
        } catch (error) {
            Alert.alert("Error", "An error occurred while logging out");
        }
    };

    return (
        <View style={styles.container}>
            <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
                <Text style={styles.logoutButtonText}>Logout</Text>
            </TouchableOpacity>
            <Text style={styles.title}>Home Page</Text>
            <View style={styles.buttonContainer}>
                <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('MainGoalPage')}>
                    <Text style={styles.buttonText}>See Goal</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('HabitsPage')}>
                    <Text style={styles.buttonText}>See Habits</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('HistoryPage')}>
                    <Text style={styles.buttonText}>See History</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('ReportPage')}>
                    <Text style={styles.buttonText}>See Last Report</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('SettingsPage')}>
                    <Text style={styles.buttonText}>Settings</Text>
                </TouchableOpacity>
            </View>
        </View>
    );
};

export default HomePage;
