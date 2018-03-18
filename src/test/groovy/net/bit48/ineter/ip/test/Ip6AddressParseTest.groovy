package net.bit48.ineter.ip.test

import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.GZIPInputStream

import net.bit48.ineter.ip.Ip6Address
import spock.lang.Specification

class Ip6AddressParseTest extends Specification{

	def "parsing and toString - simple"(){
		expect:
			def java = Inet4Address.getByName(a).toString().substring(1)
			def ineter = Ip6Address.of(a).toString()
			java.equals(ineter)
			
			where:
				a << ["abcd:dbca:1234:4321:aabb:bbaa:ccdd:ddcc", "A:B:C:D:E:F:a:B"]
	}
	
	def "parsing and toString"(){
		expect:
		def java = Inet4Address.getByName(a).toString().substring(1)
		def ineter = Ip6Address.of(a).toString()
		java.equals(ineter)

		where:
		a << ["::1", "1::", "1::1", "1:2:3:4:5:6:7:8", "12:34:56::abcd", "::","1:02:003:0004::","1000:200:30:4::"]
	}
	
	def "random ips from file - check parsing and speed"(){
		given:
		def iterations = 200
		def gzip = new GZIPInputStream(new FileInputStream("src/test/resources/IPv6_Random_Addresses.txt.gz"));
		def br = new BufferedReader(new InputStreamReader(gzip));
		def addresses = br.readLines()
		Collections.shuffle(addresses)
		def addressCount = addresses.size()
		def before, javaTotal = 0, inetTotal = 0

		when:
		def java = new InetAddress[addressCount]
		def inet = new Ip6Address[addressCount]

		for (int count =0; count<iterations; count++) {
			for (int i = 0; i < addressCount; i++) {
				before = System.nanoTime()
				java[i] = InetAddress.getByName(addresses.get(i))
				javaTotal += (System.nanoTime() - before)

				before = System.nanoTime()
				inet[i] = Ip6Address.of(addresses.get(i))
				inetTotal += (System.nanoTime() - before)
			}
		}
		then:
		for (int i=0;i<addressCount;i++){
			assert java[i].getHostAddress().toString().equals(inet[i].toString())
		}
		javaTotal >= inetTotal
	}

	def "valid brackets"(){
		expect:
		def java = Inet4Address.getByName(a).toString().substring(1)
		def ineter = Ip6Address.of(a).toString()
		java.equals(ineter)

		where:
		a << ["[::1]", "[1::]", "[1::1]", "[1:2:3:4:5:6:7:8]", "[12:34:56::abcd]", "[::]", "[abcd:dbca:1234:4321:aabb:bbaa:ccdd:ddcc]", "[A:B:C:D:E:F:a:B]"]
	}
	
	def "invalid brackets"(){
		when:
		def ineter = Ip6Address.of(a)

		then:
		thrown(IllegalArgumentException)
		
		where:
		a << ["::1]", "[1::"]
	}
	
	def "illegal addresses - length"(){
		when:
		def ineter = Ip6Address.of(a)

		then:
		IllegalArgumentException e = thrown(IllegalArgumentException.class)
		e.getMessage().toLowerCase().contains("length")

		where:
		a << ["", "1", "[0000:0000:0000:0000:0000:0000:0000:00001]",]
	}

	def "illegal addresses - Too many digits"(){
		when:
		def ineter = Ip6Address.of(a)

		then:
		IllegalArgumentException e = thrown(IllegalArgumentException.class)
		e.getMessage().toLowerCase().contains("digits")

		where:
		a << [
			"00001:0000:0000:0000:0000:0000:0000:000",
			"0000:00001:0000:0000:0000:0000:0000:000",
			"0000:0000:00001:0000:0000:0000:0000:000",
			"0000:0000:0000:00001:0000:0000:0000:000",
			"0000:0000:0000:0000:00001:0000:0000:000",
			"0000:0000:0000:0000:0000:00001:0000:000",
			"0000:0000:0000:0000:0000:0000:00001:000",
			"000:0000:0000:0000:0000:0000:0000:00001",
			"00001:",
			"0000:00001:",
			"0000:0000:00001::",
			"0000:0000:0000:00001::",
			"::00001:0000:0000:0000",
			"::00001:0000:0000",
			"::00001:0000",
			"::00001"
		]
	}

	def "illegal addresses - number of parts"(){
		when:
		def ineter = Ip6Address.of(a)

		then:
		IllegalArgumentException e = thrown(IllegalArgumentException.class)
		e.getMessage().toLowerCase().contains("parts")

		where:
		a << [
			"1:1",
			"1:1:1",
			"::1:1:1:1:1:1:1:1",
			"1:1:1:1:1:1:1:1:1",
			"1:1:1:1:1:1:1:1:1:1",
			"1:1:1:1:1:1:1:1:1:1:1",
			"1:1:1:1:1:1:1:1:1:1:1:1",
			"::1:1:1:1:1:1:1:1:1",
			"1::1:1:1:1:1:1:1:1",
			"1:1::1:1:1:1:1:1:1",
			"1:1:1::1:1:1:1:1:1",
			"1:1:1:1::1:1:1:1:1",
			"1:1:1:1:1::1:1:1:1",
			"1:1:1:1:1:1::1:1:1",
			"1:1:1:1:1:1:1::1:1",
			"1:1:1:1:1:1:1:1::1",
			"1:1:1:1:1:1:1:1:1::",
		]
	}

	def "illegal addresses - colons"(){
		when:
		def ineter = Ip6Address.of(a)

		then:
		IllegalArgumentException e = thrown(IllegalArgumentException.class)
		e.getMessage().toLowerCase().contains("colon")

		where:
		a << [
			"1:::1:1:1:1:1:1:1",
			"1:1:::1:1:1:1:1:1",
			"1:1:1:::1:1:1:1:1",
			"1:1:1:1:::1:1:1:1",
			"1:1:1:1:1:::1:1:1",
			"1:1:1:1:1:1:::1:1",
			"1:1:1:1:1:1:::1:1",
			"1:1:1:1:1:1:1:::1",
			"1:1:1:1:1:1:1:1:::",
			"1::1:1:1:1:1:1::1",
			"1:1::1:1:1:1:1::1",
			"1:1:1::1:1:1:1::1",
			"1:1:1:1::1:1:1::1",
			"1:1:1:1:1::1:1::1",
			"1:1:1:1:1:1::1::1",
			"1:1:1:1:1:1::1::1",
			"::1:1:1:1:1:1:1:1::",
		]
	}

	def static printableCharsNoDigits(){
		def digits= (' '..'}').collect({it.toCharArray()[0]})
		digits.removeIf({Character.digit(it, 16)!=-1 || it==':' || it== ']' || it=='['})
		digits
	}

	def "illegal addresses - char after colons"(){
		when:
		def ineter = Ip6Address.of(a)

		then:
		IllegalArgumentException e = thrown(IllegalArgumentException.class)
		e.getMessage().toLowerCase().contains("character")

		where:
		a << printableCharsNoDigits().collect({"1::$it"})
	}
	
	def "illegal addresses - char before colons"(){
		when:
		def ineter = Ip6Address.of(a)

		then:
		IllegalArgumentException e = thrown(IllegalArgumentException.class)
		e.getMessage().toLowerCase().contains("character")

		where:
		a << printableCharsNoDigits().collect({"1$it::"})
	}
}