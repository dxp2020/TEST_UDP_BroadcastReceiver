package cn.zelkova.zp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.util.List;

import cn.zelkova.zp.Book;
import cn.zelkova.zp.BookManager;


public class LocalService extends Service {

    private Book book = new Book("三国演义",25);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    private class LocalBinder extends BookManager.Stub{

        @Override
        public List<Book> getBooks() throws RemoteException {
            return null;
        }

        @Override
        public Book getBook() throws RemoteException {
            return book;
        }

        @Override
        public int getBookCount() throws RemoteException {
            return 0;
        }

        @Override
        public void setBookPrice(Book book, int price) throws RemoteException {

        }

        @Override
        public void setBookName(Book book, String name) throws RemoteException {
            book.setName(name);
        }

        @Override
        public void addBookIn(Book book) throws RemoteException {

        }

        @Override
        public void addBookOut(Book book) throws RemoteException {

        }

        @Override
        public void addBookInout(Book book) throws RemoteException {

        }
    }
}
