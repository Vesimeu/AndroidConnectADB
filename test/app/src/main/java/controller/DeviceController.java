package controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import dadb.AdbKeyPair;
import dadb.Dadb;
import okio.BufferedSource;
import okio.Okio;

public class DeviceController {

    private static final String TAG = "DeviceController";
    private static final int PORT = 5555; // Порт для ADB TCP подключения
    private Dadb dadb;
    private static final Logger logger = Logger.getLogger(DeviceController.class.getName());

    public DeviceController(String host, File adbKeyDir) throws IOException {
        // Создаем или загружаем ADB ключи
        File adbKey = new File(adbKeyDir, "adbkey");
        File adbPubKey = new File(adbKeyDir, "adbkey.pub");

        AdbKeyPair keyPair;
        if (!adbKey.exists() || !adbPubKey.exists()) {
            logger.info("ADB ключи не найдены, создаем новые ключи...");
            AdbKeyPair.generate(adbKey, adbPubKey);
            logger.info("ADB ключи созданы: " + adbKey.getAbsolutePath());
        }

        keyPair = AdbKeyPair.read(adbKey, adbPubKey);
        logger.info("Ключи созданы" + keyPair);
        this.dadb = Dadb.create(host, PORT, keyPair);
        logger.info("Инициализация DeviceController с хостом: " + host);
    }

    public String executeShellCommand(String command) throws IOException {
        logger.info("Выполнение команды: " + command);
        String output = dadb.shell(command).getAllOutput();
        logger.info("Результат команды: " + output);
        return output;
    }

    public void setupTcpConnection(String deviceIp) throws IOException {
        logger.info("Настройка TCP-соединения с устройством B, IP: " + deviceIp);

        // Выполняем команду tcpip для переключения устройства на TCP-подключение
        executeShellCommand("adb tcpip 5555");
        logger.info("Команда 'tcpip 5555' выполнена успешно.");

        // Подключаемся к устройству B по TCP на фиксированном порту 5555
        this.dadb = Dadb.create(deviceIp, 5555, AdbKeyPair.readDefault());
        logger.info("Подключение к устройству B по TCP установлено.");
    }

    public void startApplicationOnDeviceB() throws IOException {
        String command = "input text \"" + "DA NY NAXUY TEKST SAM VVODITSA ?!" + "\"";
        executeShellCommand(command);
        logger.info("Сигнал отправлен на устройство B.");
    }

    public void pushFile(File srcFile, String remotePath) throws IOException {
        logger.info("Отправка файла: " + srcFile.getPath() + " на устройство B по пути: " + remotePath);

        // Получаем режим доступа и время последнего изменения файла
        int mode = readMode(srcFile);
        long lastModifiedMs = srcFile.lastModified();

        // Отправляем файл на устройство
        try (BufferedSource source = Okio.buffer(Okio.source(srcFile))) {
            dadb.push(source, remotePath, mode, lastModifiedMs);
        }
        logger.info("Файл успешно отправлен на устройство B.");
    }

    public void pullFile(String remotePath, File dstFile) throws IOException {
        logger.info("Получение файла с устройства B по пути: " + remotePath + " в файл: " + dstFile.getPath());
        dadb.pull(dstFile, remotePath);
        logger.info("Файл успешно получен с устройства B.");
    }

    public void close() throws Exception {
        if (dadb != null) {
            try {
                logger.info("Закрытие подключения к ADB.");
                dadb.close();
            } catch (Exception e) {
                logger.severe("Ошибка при закрытии подключения к ADB: " + e.getMessage());
                throw new IOException("Failed to close Dadb connection", e);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    // Метод для получения режима доступа файла
    private int readMode(File file) {
        try {
            Class<?> unixFileModeClass = Class.forName("android.system.OsConstants");
            Field field = unixFileModeClass.getField("S_IFMT");
            return field.getInt(null);
        } catch (Exception e) {
            logger.warning("Не удалось прочитать режим доступа файла, использован режим по умолчанию.");
            return 0644; // Возвращаем стандартный режим доступа на случай ошибки
        }
    }
}
