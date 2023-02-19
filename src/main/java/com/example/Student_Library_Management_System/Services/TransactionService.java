package com.example.Student_Library_Management_System.Services;


import com.example.Student_Library_Management_System.DTOs.IssueBookRequestDto;
import com.example.Student_Library_Management_System.Enums.CardStatus;
import com.example.Student_Library_Management_System.Enums.TransactionStatus;
import com.example.Student_Library_Management_System.Models.Book;
import com.example.Student_Library_Management_System.Models.Card;
import com.example.Student_Library_Management_System.Models.Transactions;
import com.example.Student_Library_Management_System.Repositories.BookRepository;
import com.example.Student_Library_Management_System.Repositories.CardRepository;
import com.example.Student_Library_Management_System.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;


    @Autowired
    BookRepository bookRepository;

    @Autowired
    CardRepository cardRepository;

    public String issueBook(IssueBookRequestDto issueBookRequestDto)throws Exception{


        int bookId = issueBookRequestDto.getBookId();
        int cardId = issueBookRequestDto.getCardId();

        //Get the book Entity and card Entity ?? Why do we need this
        //We are doing this bcz we want to set the Transaction attributes...

        Book book = bookRepository.findById(bookId).get();


       Card card = cardRepository.findById(cardId).get();

       //Final goal is to make a transaction Entity, set its attribute
        //and save it.
        
        Transactions transaction = new Transactions();

        //Setting the attributes
        transaction.setBook(book);
        transaction.setCard(card);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setIssueOperation(true);
        transaction.setTransactionStatus(TransactionStatus.PENDING);


        //attribute left is success/failure
        //Check for validations
        if(book==null || book.isIssued()==true){

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            throw new Exception("Book is not available");
        }

        if(card==null || (card.getCardStatus()!= CardStatus.ACTIVATED)){

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            throw new Exception("Card is not valid");
        }



       //We have reached a success case now.

       transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        //set attributes of book

        book.setIssued(true);

        //Btw the book and transaction : bidirectional

        List<Transactions> listOfTransactionForBook = book.getListOfTransactions();
        listOfTransactionForBook.add(transaction);
        book.setListOfTransactions(listOfTransactionForBook);



        //I need to make changes in the card
        //Book and the card
        List<Book> issuedBooksForCard = card.getBooksIssued();
        issuedBooksForCard.add(book);
        card.setBooksIssued(issuedBooksForCard);


        //Card and the Transaction : bidirectional (parent class)
        List<Transactions> listOfTransactionForCard = card.getListOfTransactions();
        listOfTransactionForCard.add(transaction);
        card.setListOfTransactions(listOfTransactionForCard);




        //save the parent.
        cardRepository.save(card);
        //automatically by cascading : book and transaction will be saved.
        //Saving the parent


        return "Book issued successfully";

    }
}