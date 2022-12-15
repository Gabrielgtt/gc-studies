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
$ javac Test.java
```

Logo apoś ele ser compilado, podemos executá-lo utilizando:

```shell
$ java -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC Test
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
$ apt-get install git
```
3. Entrar na pasta e clonar o projeto, da seguinte maneira:
```shell
$ cd gc
$ git clone https://github.com/shipilev/jdk.git epsilon-jdk
$ cd epsilon-jdk
$ git checkout --track origin/epsilon-mark-compact
```
4. Agora, compilamos a JDK
> Para compilação, precisamos passar o jtreg com sua versão superior a 6, que pode ser instada com:
```shell
$ apt-get install jtreg6
```
Após isso, podemos realmente compila-lá, passando o local aonde está presente o lib/jtreg.jar em nosso sistema:
```shell
$ sh ./configure --with-debug-level=fastdebug --with-jtreg=/usr/share/jtreg
```
> Caso tenha dificuldade em achar o caminho, o comando dpkg-query -L jtreg6 e verificando qual tem como caminho final /lib/jtreg.jar, pegando assim todo o caminho que tem até a barra, no meu caso /usr/share/jtreg.
> Talvez seja necessário também a instalação do autoconf, que pode ser feita da seguinte maneira:
```shell
$ apt-get install autoconf
```
Após instalá-lo, rodamos novamente o comando do passo 4.

5. Neste ponto rodamos o make, que irá seguir uma série de passos que irão construir nosso GC:
```shell
$ make images
```
> Neste ponto, o processo pode demorar um pouco de acordo com a capacidade da máquina que está executando-o, caso veja essa demora entenda como algo normal, principalmente no arquivo **jdk.unsupported.desktop**
> Ao longo da execução pode ser pedido a instalação de algum pacote que não está presente, seguindo o passo a passo exibido no terminal é possível instalá-los.

6. Agora, podemos rodar um teste para verificar a integridade do nosso GC podemos rodar uma série de testes. Estes teste foram projetados inicialmente para o epsilon, logo não deveríamos ter problema ao executá-los.
```shell
$ CONF=linux-x86_64-server-fastdebug make images run-test TEST=gc/epsilon/
Building targets 'images run-test' in configuration 'linux-x86_64-server-fastdebug'
 * jtreg:test/hotspot/jtreg/gc/epsilon
 Running test 'jtreg:test/hotspot/jtreg/gc/epsilon'
Passed: gc/epsilon/TestAlwaysPretouch.java
Passed: gc/epsilon/TestAlignment.java
Passed: gc/epsilon/TestElasticTLAB.java
Passed: gc/epsilon/TestEpsilonEnabled.java
Passed: gc/epsilon/TestHelloWorld.java
Passed: gc/epsilon/TestLogTrace.java
Passed: gc/epsilon/TestDieDefault.java
Passed: gc/epsilon/TestDieWithOnError.java
Passed: gc/epsilon/TestMemoryPools.java
Passed: gc/epsilon/TestMaxTLAB.java
Passed: gc/epsilon/TestPrintHeapSteps.java
Passed: gc/epsilon/TestArraycopyCheckcast.java
Passed: gc/epsilon/TestClasses.java
Passed: gc/epsilon/TestUpdateCountersSteps.java
Passed: gc/epsilon/TestDieWithHeapDump.java
Passed: gc/epsilon/TestByteArrays.java
Passed: gc/epsilon/TestManyThreads.java
Passed: gc/epsilon/TestRefArrays.java
Passed: gc/epsilon/TestObjects.java
Passed: gc/epsilon/TestElasticTLABDecay.java
Passed: gc/epsilon/TestSlidingGC.java
Test results: passed: 21
TEST SUCCESS
```

Agora que vimos que sua integridade está ok, podemos também fazer testes mais sofisticados com ele, dessa maneira:
```shell
CONF=linux-x86_64-server-fastdebug make images run-test TEST=tier1 TEST_VM_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -XX:+EpsilonSlidingGC -XX:+EpsilonVerify"
SlidingGC -XX:+EpsilonVerify"
Building targets 'images run-test' in configuration 'linux-x86_64-server-fastdebug'
...
```
> A maioria dos testes irão passar, contudo é normal que alguns acabem falhando , principalmente aqueles que têm referências fracas e classes que ainda não foram carregadas.

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
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```
- Instale o docker via apt
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
