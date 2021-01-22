package org.imt.frontBancaire.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.imt.frontBancaire.Models.Account;
import org.imt.frontBancaire.Models.CreateAccount;
import org.imt.frontBancaire.Models.Order;
import org.imt.frontBancaire.Models.Transaction;
import org.imt.frontBancaire.config.MessagingConfig;
import org.json.JSONException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;


@Controller
@RequestMapping("/tpmicroserviceBancaireFront")
public class ControllerBancaire {

    private static final String URL_BACK = "http://localhost:8080/tpmicroserviceBancaire";

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @GetMapping("")
    public String getIndex() {
        return "index";
    }

    @GetMapping("/accounts")

    public String getAccounts(Model model) {
        RestTemplate restTemplate = new RestTemplate();
        List<Account> accounts= restTemplate.getForObject(URL_BACK + "/account", List.class);
        model.addAttribute("accounts", accounts);
        return "affichage";
    }

    @GetMapping("/transactions")
    public String getTransactions(Model model) throws JSONException, IOException {


		RestTemplate restTemplate = new RestTemplate();
		List<Transaction> transactions= restTemplate.getForObject(URL_BACK + "/transactions", List.class);

        model.addAttribute("transactions", transactions);
        return "transaction";
    }

    @GetMapping("/account/transactions")
    public String getTransactions(@ModelAttribute CreateAccount createAccount) {

        return "transaction_with_id";
    }
    
    @PostMapping("/account/transactions")
    public String gotTransactions(@ModelAttribute CreateAccount createAccount, Model model) {

    	RestTemplate restTemplate = new RestTemplate();
		List<Transaction> transactions= restTemplate.getForObject(URL_BACK + "/account/" + createAccount.getNom() + "/transactions", List.class);
		
        model.addAttribute("transactions", transactions);
        return "transaction";
    }

    @PostMapping("/transactions/generate100")
    public String postTransactions(Model model) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Account>> comptesResEnt = restTemplate.exchange(URL_BACK + "/account", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {
        });
        List<Account> comptes;
        if (comptesResEnt.getStatusCode().is2xxSuccessful()) {
            comptes = comptesResEnt.getBody();
        } else
            return "transaction";
        if (comptes != null && !comptes.isEmpty()) {
            IntStream.range(0, 100).forEachOrdered(n -> {
                Order order = new Order();

                Random rand = new Random();
                int randNum = rand.nextInt(comptes.size());
                Account compteA = comptes.get(randNum);
                order.setName("RAND" + n);
                order.setOrderId("ORDER_" + n);
                order.setDescription("Randomly generated transaction");
                order.setOriginId(compteA.getId());
                order.setDestinationId(comptes.get(rand.nextInt(comptes.size())).getId());
                order.setMontant(rand.nextLong());

                rabbitTemplate.convertAndSend(MessagingConfig.EXCHANGE, MessagingConfig.ROUTING_KEY, order);
            });
        }

        return getTransactions(model);
    }
    
    @PostMapping("/account/create")
    public String createdAccount(@ModelAttribute CreateAccount createAccount, BindingResult errors, Model model) {

        if (createAccount != null && createAccount.getNom() != null && !createAccount.getNom().isEmpty()) {
            Account account = new Account();
            RestTemplate restTemplate = new RestTemplate();
            account.setIBAN(createAccount.getIban());
            account.setNom(createAccount.getNom());
            HttpEntity<Account> request = new HttpEntity<>(account);

            try {
                Account response = restTemplate.postForObject(
                        URL_BACK + "/account", request, Account.class);
                System.out.println("Account created for " + response.getNom());
                model.addAttribute("is_created", true);

            } catch (HttpClientErrorException e) {
                System.out.println(e.getStatusCode());
                model.addAttribute("is_created", false);
            }

        }
        return "create_account";
    }

    @GetMapping("/account/create")
    public String createAccount(@ModelAttribute CreateAccount createAccount, BindingResult errors, Model model) {

        return "create_account";
    }

    @GetMapping("/account/delete")
    public String deleteAccount(@ModelAttribute CreateAccount createAccount, BindingResult errors, Model model) {

        model.addAttribute("is_deleted", false);
        return "delete_account";
    }

    @PostMapping("/account/delete")

    public String deletedAccount(@ModelAttribute CreateAccount createAccount, BindingResult errors, Model model) {
    	if (createAccount != null && createAccount.getNom() != null && !createAccount.getNom().isEmpty()) {
            RestTemplate restTemplate = new RestTemplate();
            try {
                restTemplate.delete(URL_BACK + "/account/"+createAccount.getNom());
                model.addAttribute("is_deleted", true);

            } catch (HttpClientErrorException e) {
                System.out.println(e.getStatusCode());
                model.addAttribute("is_deleted", false);
            }

        }
        return "delete_account";
    }


}
