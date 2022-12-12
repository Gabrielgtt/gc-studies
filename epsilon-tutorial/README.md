# Anotações sobre o estudo do EpsilonGC

Esse estudo segue os passos descritos no tutorial [Do It Yourself (OpenJDK) Garbage Collector](https://shipilev.net/jvm/diy-gc/).

## Tutorial

#### Primeiro contato com GC
O primeiro contato pode ser feito através de um teste introdutório utilizando o No-Op Garbage Collector(coletor de lixo não operacional) conhecido como Epsilon. Para isso, podemos inicialmente criar um arquivo chamado Test.java (que tambem está disponivel na pasta Codigos), utilizando o comando:

```shell
vim Test.java
```
Dentro do arquivo, colamos o seguinte código: 

```java
public class Test {

    static final int MEGABYTE_EM_BYTES = 1024 * 1024;
    static final int QUANT_INTERACOES = 1024 * 10;

    public static void main(String[] args) {
        System.out.println("Comecando a insercao");

        for (int i = 0; i < QUANT_INTERACOES; i++) {
            byte[] array = new byte[MEGABYTE_EM_BYTES];
        }

        System.out.println("Finalizando");
    }
}
```
Em seguida, é utilizado o seguinte comando para compilar o programa:

```shell
javac Test.java
```

Logo apoś ele ser compilado, podemos executá-lo utilizando:

```shell
java -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC Test
```
>Neste ponto é necessário ter atenção ao local que está a flag 
>**“-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC”**, 
pois a mesma tem que apontar para o interpretador java, e não para 
> o programa que será executado.

Como resultado esperado, temos: 
```shell
$ java-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC Test
[0.001s][warning][gc] Consider setting -Xms equal to -Xmx to avoid resizing hiccups
[0.001s][warning][gc] Consider enabling -XX:+AlwaysPreTouch to avoid memory commit hiccups
Comecando a insercao
Terminating due to java.lang.OutOfMemoryError: Java heap space
```
Isso se deve ao fato de que o Epsilon não faz operações de coleta de lixo, então quando o Heap está cheio, apenas lança o erro de OutOfMemory.

O que não ocorre quando não usamos a flag, pois até o mais simples coletor de lixo consegue “limpar” o que estamos fazendo e prosseguir com a execução normal do programa, como podemos ver no exemplo abaixo:
```shell
$ java Test
Comecando a insercao
Finalizando
```

#### Fazendo nosso próprio coletor
Para isso, utilizaremos o seguinte repositório ([Link](https://github.com/shipilev/jdk/tree/epsilon-mark-compact)). O mesmo pode ser clonado de acordo com os seguintes passos: 

1. Criar uma pasta para ter uma melhor organização, como por exemplo:
```shell
$ mkdir gc
```
2. Instalar o git:
```shell
apt-get install git
```
3. Entrar na pasta e clonar o projeto, da seguinte maneira:
```shell
$ cd gc
$ git clone https://github.com/shipilev/jdk.git epsilon-jdk
$ cd epsilon-jdk
$ git checkout --track origin/epsilon-mark-compact
```
4. Agora, criamos a OpenJDK
```shell
sh ./configure --with-debug-level=fastdebug
```
>Talvez seja necessário também a instalação do autoconf, que pode ser feita da seguinte maneira:
```shell
apt-get install autoconf
```
Após instalá-lo, rodamos novamente o comando do passo 4.

## Usando containers

Se as dependências estiverem muito quebradas, faltando ou em versões conflitantes, pode-se rodar o tutorial dentro de um container Docker. Para isso é preciso instalar as dependências docker na sua máquina local e em seguida seguir o tutorial de dentro do container criado.


### Máquina local

Na sua máquina local você precisa ter o docker instalado e criar o container.

**Instale Docker** ([Link](https://docs.docker.com/engine/install/ubuntu/))
- Dependências
```shell
sudo apt install ca-certificates curl gnupg lsb-release -y
```
- GPK key
```shell
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
```
- Setup do repositório
```shell
sudo echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
```
- Installe o docker via apt
```shell
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y
```


**Crie e entre no modo interativo no container criado**
```shell
sudo docker run --name ubuntu-test -i -t ubuntu:18.04
```

De agora em diante você está no terminal do ubuntu dentro de um container separado.


### Container

**Instale Java**
- Provavelmente isso instalará o java 11.
```shell
apt update
apt install default-jdk
```
