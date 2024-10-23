# Spring Boot and Vaadin course source code

This repository contains the source code for the [Building Modern Web Applications With Spring Boot and Vaadin](https://vaadin.com/docs/latest/flow/tutorials/in-depth-course).

*Live demo:* https://crm.demo.vaadin.com

## Running the Application
There are two ways to run the application:  using `mvn` or by running the `Application` class directly from your IDE.

## Branches

- The main branch contains the source code for the latest Vaadin release
- The `v14` branch contains the source code for Vaadin 14


## Text tutorial
You can find a text version of the tutorial in the [Vaadin Documentation](https://vaadin.com/docs/latest/flow/tutorials/in-depth-course).


## Enable HTTPS locally

#### Install mkcert
Follow any install instructions from the mkcert docs: https://github.com/FiloSottile/mkcert

#### Create a local Certificate Authority (CA)

Open an elevated (e.g. Run as Administrator) command line
Run `mkcert -install`
You should see something similar to the following:
```
- Using the local CA at “C:\Users\[Username]\AppData\Local\mkcert” ✨
- The local CA is now installed in the system trust store! ⚡️
- The local CA is now installed in Java’s trust store! ☕
```

#### Configure Spring Boot to use HTTPS

```shell
# create the directory
mkdir -p ~/.config/spring-boot

# generate a certificate in pkcs12 format
mkcert -pkcs12 -p12-file ~/.config/spring-boot/local-tls.p12 {IP-address}
```

Add to `application.properties`

```shell
server.ssl.key-store=${user.home}/.config/spring-boot/local-tls.p12
```

##### Android chrome

Go to `chrome://flags` and add `https://{IP-address}:port` to `Insecure origins treated as secure` and enable the experiment.

Navigate to self signed https app and test webpush on mobile.

generate crt from the custom pem root certificate  
`openssl x509 -inform PEM -outform DM -in rootCA.pem -out local.crt`

install the crt on the android as user certificate.

##### iOS Safari

For ipad the minimum requirement is `iOS 16.4` which got Web Push support.

To install the local certificate download it for the network and install after it has downloaded.

Then turn on SSL/TLS trust for the certificate, 
    - go to Settings > General > About > Certificate Trust Settings
    - Under "Enable full trust for root certificates," turn on trust for the certificate.

Then enable the required advanced features for Safari
    - go to Settings > Safari > Advanced > Experimental Features
    - enable `Notifications` and `Push API`

Navigate to the application with safari and through the share menu select `Add to Home Screen`

Open the site from the home screen to be able to make the push subscription.


Note! The subject needs to be the server address or a `mailto:` when using Safari and the apple web push server.
