package com.dwj.rpc.test.server;

import com.dwj.rpc.server.RpcService;
import com.dwj.rpc.test.client.Person;
import com.dwj.rpc.test.client.PersonService;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe:
 *
 * @author Seven on 2020/5/25
 */
@RpcService(PersonService.class)
public class PersonServiceImpl implements PersonService {
    @Override
    public List<Person> GetTestPerson(String name, int num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            persons.add(new Person(Integer.toString(i), name));
        }
        return persons;
    }
}
