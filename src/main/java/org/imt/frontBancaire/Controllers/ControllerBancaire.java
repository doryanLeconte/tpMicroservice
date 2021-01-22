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
    public String getAccounts(Model model) throws JSONException, IOException {

        String json = readJsonFromUrl(URL_BACK + "/account");
        ObjectMapper objMapper = new ObjectMapper();
        List<Account> accountResults = objMapper.readValue(json, List.class);

        model.addAttribute("accounts", accountResults);
        return "affichage";
    }

    @GetMapping("/transactions")
    public String getTransactions(Model model) throws JSONException, IOException {

        String json = readJsonFromUrl(URL_BACK + "/transactions");
        ObjectMapper objMapper = new ObjectMapper();
        List<Transaction> transactionResults = objMapper.readValue(json, List.class);

        model.addAttribute("transactions", transactionResults);
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

    @GetMapping("/account/{id}/transactions")
    public String getTransactions(@PathVariable(name = "id") String id, Model model) throws JSONException, IOException {

        String json = readJsonFromUrl(URL_BACK + "/account/" + id + "/transactions");
        ObjectMapper objMapper = new ObjectMapper();
        List<Transaction> transactionResults = objMapper.readValue(json, List.class);

        model.addAttribute("transactions", transactionResults);
        return "transaction";
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
    public String deletedAccount(@ModelAttribute CreateAccount createAccount, BindingResult errors, Model model) throws
            JSONException, IOException {
        Boolean boolRes = false;
        if (createAccount != null && createAccount.getNom() != null && !createAccount.getNom().isEmpty()) {
            String json = readJsonFromUrl(URL_BACK + "/account/2/" + createAccount.getNom());
            ObjectMapper objMapper = new ObjectMapper();
            boolRes = objMapper.readValue(json, Boolean.class);
        }
        model.addAttribute("is_deleted", boolRes);
        return "delete_account";
    }


    private String readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return jsonText;
        } finally {
            is.close();
        }
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
