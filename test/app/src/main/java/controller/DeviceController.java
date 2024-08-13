package controller;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import dadb.AdbKeyPair;
import dadb.Dadb;
import okio.BufferedSource;
import okio.Okio;
import timber.log.Timber;

public class DeviceController {

    private static final String TAG = "DeviceController";
    private static final int PORT = 5555; // Порт для ADB TCP подключения
    private Dadb dadb;

    public DeviceController(String host, File adbKeyDir) throws IOException {
        // Создаем или загружаем ADB ключи
        File adbKey = new File(adbKeyDir, "adbkey");
        File adbPubKey = new File(adbKeyDir, "adbkey.pub");

        AdbKeyPair keyPair;
        if (!adbKey.exists() || !adbPubKey.exists()) {
            Timber.tag(TAG).i("ADB ключи не найдены, создаем новые ключи...");
            AdbKeyPair.generate(adbKey, adbPubKey);
            Timber.tag(TAG).i("ADB ключи созданы: " + adbKey.getAbsolutePath());
        }

        keyPair = AdbKeyPair.read(adbKey, adbPubKey);
        this.dadb = Dadb.create(host, PORT, keyPair);
        Timber.tag(TAG).i("Инициализация DeviceController с хостом: " + host);
    }

    public String executeShellCommand(String command) throws IOException {
        Timber.tag(TAG).i("Выполнение команды: " + command);
        Log.i(TAG,"Выполнение команды: " + command);
        String output = dadb.shell(command).getAllOutput();
        Timber.tag(TAG).i("Результат команды: " + output);
        return output;
    }

    public void setupTcpConnection(String deviceIp) throws IOException {
        Timber.tag(TAG).i("Настройка TCP-соединения с устройством B, IP: %s", deviceIp);

        // Выполняем команду tcpip для переключения устройства на TCP-подключение
        executeShellCommand("adb tcpip 5555");
        Timber.tag(TAG).i("Команда 'tcpip 5555' выполнена успешно.");

        // Подключаемся к устройству B по TCP на фиксированном порту 5555
        this.dadb = Dadb.create(deviceIp, 5555, AdbKeyPair.readDefault());
        Timber.tag(TAG).i("Подключение к устройству B по TCP установлено.");
    }

    public void startApplicationOnDeviceB() throws IOException {
        executeShellCommand("am start -n com.Tenlab.Drone/com.unity3d.player.UnityPlayerActivity"); //Команда запуска приложения
        Timber.tag(TAG).i("Приложение на устройстве B запущено.");
    }

    public void pushFile(File srcFile, String remotePath) throws IOException {
        Timber.tag(TAG).i("Отправка файла: " + srcFile.getPath() + " на устройство B по пути: " + remotePath);

        // Получаем режим доступа и время последнего изменения файла
        int mode = readMode(srcFile);
        long lastModifiedMs = srcFile.lastModified();

        // Отправляем файл на устройство
        try (BufferedSource source = Okio.buffer(Okio.source(srcFile))) {
            dadb.push(source, remotePath, mode, lastModifiedMs);
        }
        Timber.tag(TAG).i("Файл успешно отправлен на устройство B.");
    }

    public void pullFile(String remotePath, File dstFile) throws IOException {
        Timber.tag(TAG).i("Получение файла с устройства B по пути: " + remotePath + " в файл: " + dstFile.getPath());
        dadb.pull(dstFile, remotePath);
        Timber.tag(TAG).i("Файл успешно получен с устройства B.");
    }

    public void close() throws Exception {
        if (dadb != null) {
            try {
                Timber.tag(TAG).i("Закрытие подключения к ADB.");
                dadb.close();
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "Ошибка при закрытии подключения к ADB: " + e.getMessage());
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
            Timber.tag(TAG).w("Не удалось прочитать режим доступа файла, использован режим по умолчанию.");
            return 0644; // Возвращаем стандартный режим доступа на случай ошибки
        }
    }
}
