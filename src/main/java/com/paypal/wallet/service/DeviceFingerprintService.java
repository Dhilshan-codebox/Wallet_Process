package com.paypal.wallet.service;

import com.paypal.wallet.model.DeviceInfo;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.DeviceInfoRepository;
import com.paypal.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeviceFingerprintService {

    @Autowired
    private DeviceInfoRepository deviceInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.device.alert-on-new-device:true}")
    private boolean alertOnNewDevice;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Called after successful login. Checks if the device is known,
     * registers if new, and sends an alert email.
     */
    public void processLogin(User user, String userAgent, String ipAddress) {
        String deviceHash = computeHash(userAgent + "|" + ipAddress);
        String country = resolveCountry(ipAddress);

        Optional<DeviceInfo> existingDevice = deviceInfoRepository.findByUserAndDeviceHash(user, deviceHash);

        if (existingDevice.isPresent()) {
            // Update last seen
            DeviceInfo device = existingDevice.get();
            device.setLastSeen(LocalDateTime.now());
            device.setIpAddress(ipAddress);
            device.setCountry(country);
            deviceInfoRepository.save(device);
        } else {
            // New device — register and alert
            DeviceInfo newDevice = new DeviceInfo(user, deviceHash, userAgent, ipAddress);
            newDevice.setCountry(country);
            deviceInfoRepository.save(newDevice);

            if (alertOnNewDevice) {
                sendNewDeviceAlert(user, ipAddress, userAgent, country);
            }
        }

        // Update user's last known IP and country
        String previousCountry = user.getLastKnownCountry();
        user.setLastKnownIp(ipAddress);
        user.setLastKnownCountry(country);
        userRepository.save(user);

        // Alert on suspicious location change
        if (previousCountry != null && !previousCountry.isEmpty()
                && !previousCountry.equalsIgnoreCase(country)
                && !country.equals("Unknown")) {
            sendLocationChangeAlert(user, previousCountry, country, ipAddress);
        }
    }

    /**
     * Computes a SHA-256 hash of the given input string.
     */
    public String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    /**
     * Calls ip-api.com to resolve the country from an IP address.
     */
    private String resolveCountry(String ip) {
        if (ip == null || ip.startsWith("127.") || ip.startsWith("192.168.") || ip.equals("0:0:0:0:0:0:0:1")) {
            return "Local";
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ip-api.com/json/" + ip + "?fields=country"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            // Parse country from: {"country":"India"}
            if (body.contains("\"country\"")) {
                int start = body.indexOf("\"country\"") + 11;
                int end = body.indexOf("\"", start + 1);
                if (start > 10 && end > start) {
                    return body.substring(start + 1, end);
                }
            }
        } catch (Exception e) {
            System.out.println("IP geolocation failed: " + e.getMessage());
        }
        return "Unknown";
    }

    private void sendNewDeviceAlert(User user, String ip, String userAgent, String country) {
        try {
            String body = "Hello " + user.getName() + ",\n\n"
                    + "A new device/location has logged into your WalletApp account:\n\n"
                    + "  IP Address : " + ip + "\n"
                    + "  Country    : " + country + "\n"
                    + "  Device     : "
                    + (userAgent != null ? userAgent.substring(0, Math.min(80, userAgent.length())) : "Unknown") + "\n"
                    + "  Time       : " + LocalDateTime.now() + "\n\n"
                    + "If this was you, no action is needed.\n"
                    + "If not, change your password immediately.\n\n"
                    + "WalletApp Security Team";
            emailService.sendTransactionEmail(user.getEmail(), "🔔 New Device Login Detected", body);
        } catch (Exception e) {
            System.out.println("New device alert email failed: " + e.getMessage());
        }
    }

    private void sendLocationChangeAlert(User user, String oldCountry, String newCountry, String ip) {
        try {
            String body = "Hello " + user.getName() + ",\n\n"
                    + "We detected a login from a different country than usual:\n\n"
                    + "  Previous Location: " + oldCountry + "\n"
                    + "  New Location     : " + newCountry + "\n"
                    + "  IP Address       : " + ip + "\n\n"
                    + "If this is unexpected, please secure your account immediately.\n\n"
                    + "WalletApp Security Team";
            emailService.sendTransactionEmail(user.getEmail(), "🌍 Unusual Location Login Detected", body);
        } catch (Exception e) {
            System.out.println("Location change alert email failed: " + e.getMessage());
        }
    }
}
