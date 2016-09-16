package com.example.fingerprintdemo.injects;

import com.example.fingerprintdemo.rest.StoreService;

import dagger.Component;

@Component(modules = {StoreModule.class})
public interface StoreComponent {

    StoreService provideStoreService();

}
